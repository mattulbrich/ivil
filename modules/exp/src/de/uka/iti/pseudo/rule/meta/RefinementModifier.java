package de.uka.iti.pseudo.rule.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.ProgramChanger;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.meta.RefinementMarkInfoCollector.MarkInfo;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.TermFactory;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.DefaultStatementVisitor;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.HavocStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.term.statement.StatementVisitor;
import de.uka.iti.pseudo.util.TermUtil;

final class RefinementModifier {

    private final @NonNull LiteralProgramTerm programTerm;
    private final @NonNull LiteralProgramTerm innerProgramTerm;
    private final @NonNull Term postcondition;

    private final @NonNull Function markConcreteProgvar;
    private final @NonNull Function markAbstractProgvar;
    private Map<Integer, MarkInfo> markInfoConcrete;
    private Map<Integer, MarkInfo> markInfoAbstract;
    private final Environment env;
    private final TermFactory tf;
    private final Set<Function> modifiedProgramVars = new HashSet<Function>();
    private final StatementVisitor writtenProgramVariableFinder = new DefaultStatementVisitor() {

        @Override
        public void visit(HavocStatement havocStatement) throws TermException {
            Function progvar = TermUtil.getFunction(havocStatement.getTarget());
            modifiedProgramVars.add(progvar);
        }

        @Override
        public void visit(AssignmentStatement assignmentStatement) throws TermException {
            for (Assignment ass: assignmentStatement.getAssignments()) {
                modifiedProgramVars.add(TermUtil.getFunction(ass.getTarget()));
            }
        }

        @Override
        protected void visitDefault(Statement statement) {
        }
    };

    public RefinementModifier(Environment env, Term term, Function markAbstract,
            Function markConcrete) throws TermException {

        this.env = env;
        this.tf = new TermFactory(env);

        if (term instanceof LiteralProgramTerm) {
            this.programTerm = (LiteralProgramTerm) term;
        } else {
            throw new TermException("Argument needs to be a program term");
        }

        if (programTerm.getSubterm(0) instanceof LiteralProgramTerm) {
            innerProgramTerm = (LiteralProgramTerm) programTerm.getSubterm(0);
        } else {
            throw new TermException("Post-condition needs to be a program term");
        }

        if (programTerm.getModality() != Modality.BOX) {
            throw new TermException("Outer modality needs to be [.]");
        }

        if (innerProgramTerm.getModality() != Modality.DIAMOND) {
            throw new TermException("Outer modality needs to be [<.>]");
        }

        if(markConcrete == markAbstract) {
            throw new TermException("Abstract and concrete marker are identical: " + markConcrete);
        }

        this.postcondition = innerProgramTerm.getSubterm(0);
        this.markAbstractProgvar = markAbstract;
        this.markConcreteProgvar = markConcrete;
    }

    public Term apply() throws TermException {
        assert markAbstractProgvar != null;
        assert markConcreteProgvar != null;

        collectMarkInfo();

        if(!markInfoAbstract.keySet().equals(markInfoConcrete.keySet())) {
            String msg = "The sets of used marker literals differ:\n" +
                    "Abstract (" + markAbstractProgvar + "): " + markInfoAbstract.keySet() +
                    "\nRefined (" + markConcreteProgvar + "): " + markInfoConcrete.keySet();
            throw new TermException(msg);
        }

        identifyModifiedProgVars();

        Term glueInv = prepareGlue();
        Update anonUpd = prepareAnonUpdate();
        Program concrPrime = modifyProgram(programTerm.getProgram(), markInfoConcrete.values());
        Program abstrPrime = modifyProgram(innerProgramTerm.getProgram(), markInfoAbstract.values());

        Term result = makeProofObligations(concrPrime, abstrPrime, anonUpd, glueInv);

        return result;
    }

    private Term makeProofObligations(Program concrPrime, Program abstrPrime, Update anonUpd, Term glue) throws TermException {
        int indexAbs = innerProgramTerm.getProgramIndex();
        LiteralProgramTerm a = LiteralProgramTerm.getInst(indexAbs,
                innerProgramTerm.getModality(), abstrPrime, glue);

        int indexConcr = programTerm.getProgramIndex();
        LiteralProgramTerm c = LiteralProgramTerm.getInst(indexConcr,
                programTerm.getModality(), concrPrime, a);

        // no update for 0
        // Term result = tf.upd(anonUpd, c);
        Term result = c;

        for (Integer literal : markInfoAbstract.keySet()) {
            indexAbs = markInfoAbstract.get(literal).index;
            a = LiteralProgramTerm.getInst(indexAbs + 2,
                    innerProgramTerm.getModality(), abstrPrime, glue);

            indexConcr = markInfoConcrete.get(literal).index;
            c = LiteralProgramTerm.getInst(indexConcr + 2,
                    programTerm.getModality(), concrPrime, a);


            Term term = tf.upd(anonUpd, c);
            result = tf.and(result, term);
        }
        return result;
    }

    private void identifyModifiedProgVars() throws TermException {
        programTerm.getProgram().visitStatements(writtenProgramVariableFinder);
        innerProgramTerm.getProgram().visitStatements(writtenProgramVariableFinder);
        modifiedProgramVars.remove(markAbstractProgvar);
        modifiedProgramVars.remove(markConcreteProgvar);
    }

    private void collectMarkInfo() throws TermException {
        this.markInfoConcrete =
                RefinementMarkInfoCollector.collectMarkAssignments(programTerm.getProgram(),
                        markConcreteProgvar);

        this.markInfoAbstract =
                RefinementMarkInfoCollector.collectMarkAssignments(innerProgramTerm.getProgram(),
                        markAbstractProgvar);
    }

    private Program modifyProgram(Program program, Collection<MarkInfo> infos) throws TermException {
        ProgramChanger pc = new ProgramChanger(program, env);
        for (MarkInfo markInfo : infos) {
            int index = markInfo.index;
            int line = pc.getStatementAt(index).getSourceLineNumber();
            pc.replaceAt(index + 1, new EndStatement(line));
        }

        Program result;
        String name = env.createNewProgramName(program.getName());
        try {
            result = pc.makeProgram(name);
            env.addProgram(result);
        } catch (EnvironmentException e) {
            throw new TermException("Exception while rewriting program " + program.getName(), e);
        }

        return result;
    }

    private Update prepareAnonUpdate() throws TermException {
        List<Assignment> assignments = new ArrayList<Assignment>();
        for (Function progVar : modifiedProgramVars) {
            try {
                String newName = env.createNewFunctionName(progVar.getName());
                Function skolem = new Function(newName, progVar.getResultType(), new Type[0],
                        false, false, ASTLocatedElement.CREATED);
                env.addFunction(skolem);
                assignments.add(new Assignment(tf.cons(progVar), tf.cons(skolem)));
            } catch(EnvironmentException e) {
                throw new TermException("Exception while preparing anonymising update", e);
            }
        }

        assignments.add(new Assignment(tf.cons(markConcreteProgvar), tf.number(0)));
        assignments.add(new Assignment(tf.cons(markAbstractProgvar), tf.number(0)));

        return new Update(assignments);
    }

    private Term prepareGlue() throws TermException {
        Term result = tf.eq(tf.cons(markAbstractProgvar), tf.cons(markConcreteProgvar));

        // the abstract mark info contains the coupling invariants
        for (MarkInfo info : markInfoAbstract.values()) {
            if(info.couplingInvariant != null) {
                Term eq = tf.eq(tf.cons(markAbstractProgvar), tf.number(info.literal));
                Term impl = tf.impl(eq, info.couplingInvariant);
                result = tf.and(result, impl);
            }
        }

        // and finally the postcondition (as "0")
        {
            Term eq = tf.eq(tf.cons(markAbstractProgvar), tf.number(0));
            Term impl = tf.impl(eq, postcondition);
            result = tf.and(result, impl);
        }

        return result;
    }

    /**
     * Get the concrete mark information. For testing purposes.
     *
     * @return the markInfoConcrete
     */
    public Map<Integer, MarkInfo> getMarkInfoConcrete() {
        return Collections.unmodifiableMap(markInfoConcrete);
    }

    /**
     * @return the markInfoAbstract
     */
    public Map<Integer, MarkInfo> getMarkInfoAbstract() {
        return Collections.unmodifiableMap(markInfoAbstract);
    }

}