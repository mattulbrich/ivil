/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.ProgramChanger;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermFactory;
import de.uka.iti.pseudo.term.statement.AssertStatement;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.HavocStatement;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.term.statement.StatementVisitor;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * Modify a program such that it can be used in an invariant rule application.
 *
 * <h2>Arguments</h2> The meta function expects 3 Arguments:
 * <ol>
 * <li>A literal program term (with some statement) <code>[n ; P ]</code>
 * <li>a boolean term to be used as invariant <code>%inv</code>.
 * <li>a term to be used as variant <code>%var</code>.
 * </ol>
 *
 * <p>
 * If the variant is the integer 0, only partial correctness will be treated.
 * This implementation does, however, not look at the termination type of
 * modality! You need to distinguish this in your rules.
 *
 * <h2>Procedure</h2>
 *
 * given an arbitrary program <code>P</code> and a position <code>n</code>
 * within its bounds, the meta function will modify the code to a program
 * <code>P'</code> with:
 *
 * <pre>
 * n+0  : assert %var &amp;&lt; old_var
 * n+1  : end %inv
 * n+2  : havoc v1
 * n+3  : havoc v2
 *        . . . all collected changed local vars (see below)
 * n+k  : assume %inv
 * n+k+1: old_var := %var
 * n+k+2: [n ; P]  ... the statement originally at n.
 * </pre>
 *
 * the resulting term is then <code>[n+2 ; P']</code>. All references to
 * statements are updated in P' since extra statements are inserted
 *
 * <h2>Collecting changed local variables</h2>
 *
 * To determine which local variables need to be havoc'ed a simple forward flow
 * analysis (depth first search) is performed which covers all possible paths
 * from [n;P] back to [n;P]. All assigned variables are collected and havoced.
 *
 * <p>
 * <b>Please note:</b> The resulting term is only good for the induction step.
 * For the base case you need to proff that the (same) invariant holds in the
 * beginning. This is not covered by this class.
 *
 * <h2>Domain of the variant</h2>
 *
 * In early versions, the variant needed to be an integer value, now this can be
 * an arbitrary term. The general "precedes" predicate <tt>$prec</tt> (as infix
 * operator {@code &<}) is used as comparison operator.
 *
 * <p>
 * It is up to the definition of taclets for this predicate for particular types
 * to handle it accordingly. For instance:
 *
 * <table border="1" cellpadding="5">
 * <tr>
 * <th>Type</th>
 * <th>{@code a &< b} becomes</th>
 * </tr>
 * <tr>
 * <td><tt>int</tt>
 * <td>{@code 0 <= a & a < b}</td>
 * </tr>
 * <tr>
 * <td><tt>set('a)</tt>
 * <td>{@code finite(b) & a <: b}</td>
 * </tr>
 * <tr>
 * <td><tt>pair('a, 'b)</tt>
 * <td>{@code fst(a) &< fst(b) | fst(a)=fst(b) & snd(a) &< snd(b)}</td>
 * </tr>
 * </table>
 *
 * <h2>Technical ...</h2>
 *
 * The meta function itself delegates most of the work to the class
 * {@link LoopModifier} of which it creates an instance during evaluation. This
 * is done so that the meta function itself does not hold any state and can be
 * evaluated even on different threads.
 *
 * This class is package readable to allow access for unit tests.
 *
 * TODO Add program name to ruleapps properties for replayability
 * Proofs can not be saved if the name of the generated program
 * is not deterministic
 */
public class LoopInvariantProgramModificationMetaFunction extends MetaFunction {

    private static final Type BOOL = Environment.getBoolType();

    public LoopInvariantProgramModificationMetaFunction() throws EnvironmentException {
        super(BOOL, "$$loopInvPrgMod", BOOL, BOOL, TypeVariable.ALPHA);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {

        LiteralProgramTerm programTerm;
        if (application.getSubterm(0) instanceof LiteralProgramTerm) {
            programTerm = (LiteralProgramTerm) application.getSubterm(0);
        } else {
            throw new TermException("First argument needs to be a program term");
        }

        Term invariant = application.getSubterm(1);
        Term variant = application.getSubterm(2);

        Application zero = Application.getInst(env.getNumberLiteral("0"), Environment.getIntType());

        // "0" as variant means no variant.
        if(zero.equals(variant)) {
            variant = null;
        }

        // use an external object so that no state is stored in the meta
        // function
        LoopModifier modifier = new LoopModifier(programTerm, invariant, variant, env);

        try {
            return modifier.apply();
        } catch (EnvironmentException e) {
            throw new TermException(e);
        }
    }
}


class LoopModifier {

    private static final boolean SPLIT_INVARIANT =
            Settings.getInstance().getBoolean("pseudo.loopAssertionSplit", true);

    private final Set<Function> modifiedAssignables = new HashSet<Function>();
    private Term varAtPre;
    private final LiteralProgramTerm programTerm;

    private final Term invariant;
    private final Term variant;
    private final Environment env;
    private final TermFactory tf;
    private ProgramChanger programChanger;
    private final Program originalProgram;

    public LoopModifier(LiteralProgramTerm programTerm, Term invariant, Term variant, Environment env) {
        this.programTerm = programTerm;
        this.originalProgram = programTerm.getProgram();
        this.invariant = invariant;
        this.variant = variant;
        this.env = env;
        this.tf = new TermFactory(env);
    }

    // package default to unit test it.
    LiteralProgramTerm apply() throws TermException, EnvironmentException {

        Program program = programTerm.getProgram();
        int index = programTerm.getProgramIndex();

        if(index > program.countStatements()) {
            throw new EnvironmentException(
                    "Try to apply loop transformation outside the bounds of a program, " +
                    "better just symbolically execute the statement: " + programTerm);
        }

        collectAssignables();

        programChanger = new ProgramChanger(program, env);

        makeVarAtPreSymbol();

        index = insertProofObligations(index);
        removeSkip(index);
        insertAssumptions(index);

        String name = env.createNewProgramName(program.getName());
        Program newProgram = programChanger.makeProgram(name);
        env.addProgram(newProgram);

        LiteralProgramTerm newProgramTerm =
            LiteralProgramTerm.getInst(index, programTerm.getModality(), newProgram, programTerm.getSuffixTerm());

        return newProgramTerm;

    }

    /**
     * Collect all assignables starting from the programTerm.
     *
     * The result is stored in {@link #modifiedAssignables}. This is performed
     * as follows:
     * <ol>
     * <li>Build a predecessor table <code>predecTable</code>which holds for
     * every statement in the program the statements which are predecessors (in
     * the control flow).
     * <li>Using that, calculate the set <code>reachingStatements</code> of
     * statements which reach the index of the {@link #programTerm}.
     * <li>Check which statements within <code>reachingStatements</code> are
     * reachable from the {@link #programTerm}. Add their assigned program
     * variables to {@link #modifiedAssignables}. </ul>
     *
     * @throws TermException
     *             if something goes wrong in the analyser
     */
    // package default to unit test it.
    void collectAssignables() throws TermException {
        Collection<Integer>[] predecTable = makePredecessorTable();
        Collection<Integer> reachingStatements = makeReachingList(predecTable);
        visitReachable(reachingStatements, programTerm.getProgramIndex());
    }

    /**
     * Make a table recording the predecessors of nodes.
     *
     * Using a {@link StatementAnalyser}, walk other the
     * {@link #originalProgram} and record for each statement its predecessors.
     *
     * While the collections must not contain <code>null</code> entried, the
     * array may have <code>null</code>-entries. They correspond to nodes with
     * no predecessors.
     *
     * @return a freshly created array of integer collections. <b>May contain
     *         <code>null</code></b>
     *
     * @throws TermException
     *             if the analyser fails.
     */
    @SuppressWarnings("unchecked")
    private Collection<Integer>[] makePredecessorTable() throws TermException {
        int progSize = originalProgram.countStatements();
        Collection<Integer>[] predecessorTable = (Collection<Integer>[]) new Collection<?>[progSize];

        for (int i = 0; i < progSize; i++) {
            StatementAnalyser analyser = new StatementAnalyser(i);
            originalProgram.getStatement(i).visit(analyser);
            for (int target : analyser.successorIndices) {
                // new target within bounds?
                if(target < progSize) {
                    if(predecessorTable[target] == null) {
                        predecessorTable[target] = new LinkedList<Integer>();
                    }
                    predecessorTable[target].add(i);
                }
            }
        }
        return predecessorTable;
    }

    /**
     * Using the predecessor table, make a list of those statements which reach
     * the start index.
     *
     * @param predecTable
     *            result of {@link #makePredecessorTable()}
     *
     * @return a freshly created list of statement indices.
     */
    private Collection<Integer> makeReachingList(Collection<Integer>[] predecTable) {
        int start = programTerm.getProgramIndex();
        Collection<Integer> reachingStatements = new HashSet<Integer>();

        Collection<Integer> predecs = predecTable[start];
        if (predecs != null) {
            for (int r : predecs) {
                makeReachingList0(reachingStatements, predecTable, r);
            }
        }

        return reachingStatements;
    }

    /*
     * used for recursion.
     */
    private void makeReachingList0(Collection<Integer> reachingStatements,
            Collection<Integer>[] predecTable, int stm) {

        if(reachingStatements.contains(stm)) {
            return;
        }

        reachingStatements.add(stm);

        Collection<Integer> predecs = predecTable[stm];
        if (predecs != null) {
            for (int r : predecs) {
                makeReachingList0(reachingStatements, predecTable, r);
            }
        }
    }

    /**
     * Visit all statements which are reachable from {@link #programTerm}.
     *
     * If the 2nd argument is in the 1st, then analyse the statements: Add the
     * modified assignables to {@link #modifiedAssignables} and analyse it
     * descendants.
     *
     * @param reachingStatements
     *            the result of {@link #makeReachingList(Collection[])}
     * @param index
     *            the index to investigate
     *
     * @throws TermException
     *             if the analyser fails.
     */
    private void visitReachable(Collection<Integer> reachingStatements, int index) throws TermException {

        if (!reachingStatements.contains(index)) {
            return;
        }

        StatementAnalyser analyser = new StatementAnalyser(index);
        originalProgram.getStatement(index).visit(analyser);
        modifiedAssignables.addAll(analyser.assignedVars);

        // no need to come here again!
        reachingStatements.remove(index);

        for (Integer successor : analyser.successorIndices) {
            visitReachable(reachingStatements, successor);
        }
    }


    /*
     * create a new symbol for the variant in prestate only if there is a
     * variant (variant != null) and a term has not yet been set from outside
     * (e.g. during testing).
     */
    private void makeVarAtPreSymbol() throws TermException, EnvironmentException {
        if(varAtPre == null && variant != null) {
            String newname = env.createNewFunctionName("varAtPre");
            Function varAtPreSym = new Function(newname, variant.getType(), new Type[0],
                    false, false, ASTLocatedElement.CREATED);
            try {
                env.addFunction(varAtPreSym);
                varAtPre = Application.getInst(varAtPreSym, varAtPreSym.getResultType());
            } catch (EnvironmentException e) {
                throw new TermException(e);
            }
        }
    }

    private int insertProofObligations(int index) throws TermException {
        int sourceLineNumber = programTerm.getStatement().getSourceLineNumber();

        List<AssertStatement> assertions =
                makeInvariantAssertions(sourceLineNumber);

        for (AssertStatement assertion : assertions) {
            programChanger.insertAt(index, assertion, "Continuation preserves invariant");
            index++;
        }

        // old integer only solution
//        if (variant != null) {
//            Term varGt0 = tf.gte(variant, tf.number(0));
//            Term varLtVar0 = tf.lt(variant, varAtPre);
//            AssertStatement assertion = new AssertStatement(sourceLineNumber, tf.and(varGt0, varLtVar0));
//            programChanger.insertAt(index, assertion, "Continuation reduces variant");
//            index++;
//        }

        if (variant != null) {
          Term prec = tf.prec(variant, varAtPre);
          AssertStatement assertion = new AssertStatement(sourceLineNumber, prec);
          programChanger.insertAt(index, assertion, "Continuation reduces variant");
          index++;
        }

        // was: programChanger.insertAt(index, new EndStatement(sourceLineNumber, Environment.getTrue()));
        programChanger.insertAt(index, new AssumeStatement(sourceLineNumber, Environment.getFalse()));
        index ++;

        return index;
    }

    private List<AssertStatement> makeInvariantAssertions(int sourceLineNumber)
            throws TermException {
        if(!SPLIT_INVARIANT) {
            return Collections.singletonList(new AssertStatement(sourceLineNumber, invariant));
        }

        ArrayList<AssertStatement> result = new ArrayList<AssertStatement>();
        splitConjunction(invariant, result, sourceLineNumber);
        return result;
    }

    private void splitConjunction(Term formula,
            ArrayList<AssertStatement> result, int sourceLineNumber) throws TermException {
        if (formula instanceof Application) {
            Function function = ((Application)formula).getFunction();
            if("$and".equals(function.getName())) {
                splitConjunction(formula.getSubterm(0), result, sourceLineNumber);
                splitConjunction(formula.getSubterm(1), result, sourceLineNumber);
                return;
            }
        }
        result.add(new AssertStatement(sourceLineNumber, formula));
    }

    private void insertAssumptions(int index) throws TermException {
        int sourceLineNumber = programTerm.getStatement().getSourceLineNumber();

        for (Function f : modifiedAssignables) {
            programChanger.insertAt(index, new HavocStatement(sourceLineNumber, tf.cons(f)));
            index++;
        }

        if (variant != null) {
            programChanger.insertAt(index, new AssumeStatement(sourceLineNumber, tf.eq(varAtPre, variant)));
            index ++;
        }

        programChanger.insertAt(index, new AssumeStatement(sourceLineNumber, invariant));
        index ++;

    }

    /*
     * if the original statement was a skip it ought to be removed so that
     * the invariant is not applied again and again.
     */
    private void removeSkip(int index) throws TermException {
        Statement statementAt = programChanger.getStatementAt(index);
        if(statementAt instanceof SkipStatement) {
            programChanger.deleteAt(index);
        }
    }

    public Set<Function> getModifiedAssignables() {
        return Collections.unmodifiableSet(modifiedAssignables);
    }

    public void setVarAtPre(Term varAtPre) {
        this.varAtPre = varAtPre;
    }

    public Term getVarAtPre() {
        return varAtPre;
    }

}

class StatementAnalyser implements StatementVisitor {

    int startIndex;
    int[] successorIndices;
    List<Function> assignedVars = Collections.emptyList();

    public StatementAnalyser(int startIndex) {
        this.startIndex = startIndex;
    }

    @Override public void visit(AssertStatement assertStatement)
            throws TermException {
        successorIndices = new int[] { startIndex + 1 };
    }

    @Override public void visit(AssignmentStatement assignmentStatement)
            throws TermException {
        successorIndices = new int[] { startIndex + 1 };
        assignedVars = assignmentStatement.getAssignedVars();
    }

    @Override public void visit(AssumeStatement assumeStatement)
            throws TermException {
        // special casing the assume statement:
        // if it is assume false, it has no successor state!
        if(Environment.getFalse().equals(assumeStatement.getSubterms().get(0))) {
            successorIndices = new int[0];
        } else {
            successorIndices = new int[] { startIndex + 1 };
        }
    }

    @Override public void visit(EndStatement endStatement) throws TermException {
        successorIndices = new int[0];
    }

    @Override public void visit(GotoStatement gotoStatement)
            throws TermException {
        List<Term> targets = gotoStatement.getSubterms();
        successorIndices = new int[targets.size()];
        for (int i = 0; i < successorIndices.length; i++) {
            // we know that only integer constants are stored in these gotos here!
            Application appl = (Application) targets.get(i);
            NumberLiteral literal = (NumberLiteral) appl.getFunction();
            successorIndices[i] = literal.getValue().intValue();
        }
    }

    @Override public void visit(SkipStatement skipStatement)
            throws TermException {
        successorIndices = new int[] { startIndex + 1 };
    }

    @Override public void visit(HavocStatement havocStatement)
            throws TermException {
        successorIndices = new int[] { startIndex + 1 };
        assignedVars = Collections.singletonList(((Application)havocStatement.getTarget()).getFunction());
    }

}
