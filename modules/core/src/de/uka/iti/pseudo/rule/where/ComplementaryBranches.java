package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.TermUtil;

public class ComplementaryBranches extends WhereCondition {

    private static final Type[] EXPECTED_ARGS = {
        Environment.getBoolType(),
        Environment.getIntType(),
        Environment.getBoolType(),
        Environment.getIntType(),
        Environment.getBoolType()
    };

    public ComplementaryBranches() {
        super("complementaryBranches");
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length != EXPECTED_ARGS.length) {
            throw new RuleException("complementaryBranches expects " +
                        EXPECTED_ARGS.length + "arguments");
        }

        for (int i = 0; i < arguments.length; i++) {
            Type type = arguments[i].getType();
            if(!type.equals(EXPECTED_ARGS[i])) {
                throw new RuleException("complementaryBranches: argument " +
                            i + " must be of type " + EXPECTED_ARGS[i]);
            }
        }

    }
    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {

        return applyCondition(actualArguments, null);

    }

    @Override
    public void addInstantiations(TermMatcher termMatcher, Term[] arguments) throws RuleException {
        Term[] actualArguments = new Term[arguments.length];
        for (int i = 0; i < actualArguments.length; i++) {
            try {
                actualArguments[i] = termMatcher.instantiate(arguments[i]);
            } catch (TermException e) {
                throw new RuleException(e);
            }
        }

        Object rewind = termMatcher.getRewindPosition();
        if(!applyCondition(actualArguments, termMatcher)) {
            termMatcher.rewindTo(rewind);
        }
    }

    private boolean applyCondition(Term[] actualArguments, TermMatcher termMatcher) throws RuleException {

        Term programTerm = actualArguments[0];
        Term indexTerm1 = actualArguments[1];
        Term formula1 = actualArguments[2];
        Term indexTerm2 = actualArguments[3];
        Term formula2 = actualArguments[4];
        Program program;

        if (programTerm instanceof LiteralProgramTerm) {
            LiteralProgramTerm progTerm = (LiteralProgramTerm) programTerm;
            program = progTerm.getProgram();
        } else {
            throw new RuleException("complementaryBranches: argument 1 must be a program term");
        }

        try {
            int index1 = TermUtil.getIntLiteral(indexTerm1);
            int index2 = TermUtil.getIntLiteral(indexTerm2);

            Statement stm1 = program.getStatement(index1);
            Statement stm2 = program.getStatement(index2);

            //
            // index1 must point to smt1: assume formula1
            Term assumption1 = null;
            if (stm1 instanceof AssumeStatement) {
                AssumeStatement assume = (AssumeStatement) stm1;
                assumption1 = assume.getSubterms().get(0);
                if(!testEqual(termMatcher, formula1, assumption1)) {
                    return false;
                }
            } else {
                return false;
            }

            //
            // index2 must point to smt2: assume formula2
            Term assumption2 = null;
            if (stm2 instanceof AssumeStatement) {
                AssumeStatement assume = (AssumeStatement) stm2;
                assumption2 = assume.getSubterms().get(0);
                if(!testEqual(termMatcher, formula2, assumption2)) {
                    return false;
                }
            } else {
                return false;
            }

            //
            // formula1 and formula2 must be complementary
            if(TermUtil.isNegation(assumption1)) {
                if(assumption1.getSubterm(0).equals(assumption2)) {
                    // yes they are complementary
                    return true;
                }
            }

            //
            // formula1 and formula2 must be complementary
            if(TermUtil.isNegation(assumption2)) {
                if(assumption2.getSubterm(0).equals(assumption1)) {
                    // yes they are complementary
                    return true;
                }
            }

            return false;

        } catch (TermException e) {
            throw new RuleException("complementaryBranches: goto indices must be literals", e);
        }
    }

    public boolean testEqual(TermMatcher termMatcher, Term formula, Term assumption) {
        if (termMatcher == null) {
            if (!assumption.equals(formula)) {
                return false;
            }
        } else {
            if (!termMatcher.leftMatch(formula, assumption)) {
                return false;
            }
        }
        return true;
    }

}
