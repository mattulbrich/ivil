package de.uka.iti.pseudo.rule.meta;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.AssertStatement;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.HavocStatement;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.term.statement.StatementVisitor;

class RefinementMarkInfoCollector implements StatementVisitor {

    public static class MarkInfo {
        public final int index;
        public final int literal;
        public final Term couplingInvariant;

        public MarkInfo(int index, int literal, Term couplingInvariant) {
            this.index = index;
            this.literal = literal;
            this.couplingInvariant = couplingInvariant;
        }
    }

    private int cur;
    private int lastMarkAssignment = Integer.MIN_VALUE;
    private final List<Integer> indices = new ArrayList<Integer>();
    private final List<Integer> literals = new ArrayList<Integer>();
    private final List<Term> couplingInvariants = new ArrayList<Term>();
    private final Function markProgvar;
    private final Set<Integer> gotoTargets = new HashSet<Integer>();

    private RefinementMarkInfoCollector(Function progVar) {
        this.markProgvar = progVar;
    }

    public static List<MarkInfo> collectMarkAssignments(Program program, Function progVar) throws TermException {
        RefinementMarkInfoCollector collector = new RefinementMarkInfoCollector(progVar);
        for (Statement stm : program.getStatements()) {
            stm.visit(collector);
        }

        return collector.collectInfo();
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement)
            throws TermException {

        // must not appear in parallel assignments
        if(assignmentStatement.getAssignments().size() > 1 &&
                assignmentStatement.getAssignedVars().contains(markProgvar)) {
            throw new TermException("mark assignments must be individual assignments");
        }

        Assignment ass = assignmentStatement.getAssignments().get(0);
        Term target = ass.getTarget();
        if (target instanceof Application &&
                ((Application) target).getFunction() == markProgvar) {
            indices.add(cur);

            Term value = ass.getValue();
            int intValue = getIntLiteral(value);
            literals.add(intValue);
            lastMarkAssignment = cur;
        }

        cur ++;
    }


    @Override
    public void visit(SkipStatement skipStatement) throws TermException {
        if(lastMarkAssignment == cur - 1) {
            // a follow up skip!
            List<Term> args = skipStatement.getSubterms();
            if(args.isEmpty()) {
                couplingInvariants.add(null);
            } else {
                couplingInvariants.add(args.get(0));
            }
        }
        cur ++;
    }

    @Override
    public void visit(HavocStatement havocStatement) throws TermException {
        Term target = havocStatement.getTarget();
        if(target instanceof Application &&
                ((Application)target).getFunction() == markProgvar) {
            throw new TermException("mark progvar must not be used in havoc statements");
        }
        cur ++;
    }

    @Override
    public void visit(AssertStatement assertStatement) throws TermException {
        // must not follow an index update
        if (lastMarkAssignment == cur - 1) {
            throw new TermException("assert statement after marking statement");
        }
        cur++;
    }

    @Override
    public void visit(AssumeStatement assumeStatement) throws TermException {

        // must not follow an index update
        if (lastMarkAssignment == cur - 1) {
            throw new TermException("assume statement after marking statement");
        }
        cur++;
    }

    @Override
    public void visit(EndStatement endStatement) throws TermException {
        // must not follow an index update
        if (lastMarkAssignment == cur - 1) {
            throw new TermException("end statement after marking statement");
        }
        cur++;
    }

    @Override
    public void visit(GotoStatement gotoStatement) throws TermException {
        // must not follow an index update
        if (lastMarkAssignment == cur - 1) {
            throw new TermException("goto statement after marking statement");
        }
        // ensure that no goto statement leads to the after-mark-skip
        // statements. They may lead to the marks however
        for (Term t : gotoStatement.getSubterms()) {
            gotoTargets.add(getIntLiteral(t));
        }
        cur++;
    }

    private static int getIntLiteral(Term value) throws TermException {
        if(!(value instanceof Application)) {
            throw new TermException("expected a number literal, not " + value);
        }

        Function func = ((Application)value).getFunction();
        if(!(func instanceof NumberLiteral)) {
            throw new TermException("expected a number literal, not " + value);
        }

        // silently assuming it is in integer range ...
        int intValue = ((NumberLiteral)func).getValue().intValue();
        return intValue;
    }


    private List<MarkInfo> collectInfo() throws TermException {
        if(couplingInvariants.size() != indices.size()) {
            throw new TermException("Missing skip after last mark assignment");
        }

        BitSet literalsSeen = new BitSet();
        ArrayList<MarkInfo> result = new ArrayList<MarkInfo>();
        for (int i = 0; i < indices.size(); i++) {
            Integer index = indices.get(i);
            Integer literal = literals.get(i);

            if(gotoTargets.contains(index+1)) {
                throw new TermException("Jumping into a post-mark skip!");
            }

            if(literalsSeen.get(literal)) {
                throw new TermException("Literal has already been assigned: " + literal);
            }

            result.add(new MarkInfo(index,literal,couplingInvariants.get(i)));
        }

        return result;
    }
}
