/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import nonnull.Nullable;

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

/**
 * Modify a program such that it can be used in an invariant rule application.
 * 
 * <h2>Arguments</h2>
 * The meta function expects 3 Arguments:
 * <ol>
 * <li>A literal program term (with some statement) <code>[n ; P ]</code>
 * <li>a boolean term to be used as invariant <code>%inv</code>.
 * <li>an integer term to be used as variant <code>%var</code>.
 * </ul>
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
 * n+0  : assert %var &gt;= 0 &amp; %var &lt; old_var 
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
 * <b>Please note</b>The resulting term is only good for the induction step.
 * For the base case you need to proff that the (same) invariant holds in the
 * beginning. This is not covered by this class.
 * 
 * 
 * <h2>Technical ...</h2>
 * 
 * The meta function itself delegates most of the work to the class
 * {@link LoopModifier} of which it creates an instance during evaluation.
 * This is done so that the meta function itself does not hold any state
 * and can be evaluated even on different threads.
 * 
 * This class is package readable to allow access for unit tests. 
 */
public class LoopInvariantProgramModificationMetaFunction extends MetaFunction {
    
    private static final Type BOOL = Environment.getBoolType();
    private static final Type INT= Environment.getIntType();
    
    public LoopInvariantProgramModificationMetaFunction() throws EnvironmentException {
        super(BOOL, "$$loopInvPrgMod", BOOL, BOOL, INT);
    }
    
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
        
        Application zero = new Application(env.getNumberLiteral("0"), Environment.getIntType());
        
        // "0" as variant means no variant.
        if(zero.equals(variant))
            variant = null;
        
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

    private Set<Function> modifiedAssignables = new HashSet<Function>();
    private Term varAtPre;
    private LiteralProgramTerm programTerm;

    private Term invariant;
    private Term variant;
    private Environment env;
    private TermFactory tf;
    private ProgramChanger programChanger;
    private Program originalProgram;

    public LoopModifier(LiteralProgramTerm programTerm, Term invariant,
            Term variant, Environment env) {
        this.programTerm = programTerm;
        this.originalProgram = programTerm.getProgram();
        this.invariant = invariant;
        this.variant = variant;
        this.env = env;
        this.tf = new TermFactory(env);
    }

    // package default to unit test it.
    LiteralProgramTerm apply() throws TermException, EnvironmentException {
        collectAssignables();
        
        Program program = programTerm.getProgram();
        int index = programTerm.getProgramIndex();
        
        programChanger = new ProgramChanger(program, env);
        
        makeVarAtPreSymbol();

        index = insertProofObligations(index);
        removeSkip(index);
        insertAssumptions(index);

        String name = env.createNewProgramName(program.getName());
        Program newProgram = programChanger.makeProgram(name);
        env.addProgram(newProgram);
        
        LiteralProgramTerm newProgramTerm = new LiteralProgramTerm(index, programTerm.isTerminating(), newProgram);
        
        return newProgramTerm;
        
    }

    /**
     * collect all assignables starting from the programTerm.
     * The result is stored in {@link #modifiedAssignables}.
     */
    // package default to unit test it.
    void collectAssignables() {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(programTerm.getProgramIndex());
        collectAssignables0(stack);
    }

    /*
     * depth first search using an explicit stack. If a loop is found return
     * either true or false. If not descent on the successors. Add assigned
     * variable to set of modified variables if in a loop.
     */
    private boolean collectAssignables0(Stack<Integer> stack) {
        int size = stack.size();
        int programSize = originalProgram.countStatements();
        
        assert size >= 1;
        assert size <= programSize + 1;
        
        int peek = stack.peek();
        
        // if the index is beyond the upper bound, it will never come back
        if(peek >= programSize) {
            return false;
        }
        
        // checks only if not at the beginning
        if(size > 1) {
            
            // find the first occurrence of the top of the stack 
            int indexPeek = stack.indexOf(peek);
            
            // we found a loop to the starting point ==> return true
            if(indexPeek == 0)
                return true;
        
            // we have run into some other loop ==> return false 
            if(indexPeek < size - 1)
                return false;
        }
        
        // does one successor have a loop?
        boolean hasLoop = false;
        
        StatementAnalyser analyser = new StatementAnalyser(peek);
        try {
            originalProgram.getStatement(peek).visit(analyser);
        } catch (TermException e) {
            // never thrown in this code
            throw new Error(e);
        }
        
        for (int successor : analyser.successorIndices) {
            stack.push(successor);
            hasLoop |= collectAssignables0(stack);
            stack.pop();
        }
        
        if(hasLoop && analyser.assignedVars != null)
            modifiedAssignables.addAll(analyser.assignedVars);
        
        return hasLoop;
        
    }

    /*
     * create a new symbol for the variant in prestate only if there is a
     * variant (variant != null) and a term has not yet been set from outside
     * (e.g. during testing).
     */
    private void makeVarAtPreSymbol() throws TermException, EnvironmentException {
        if(varAtPre == null && variant != null) {
            String newname = env.createNewFunctionName("varAtPre");
            Function varAtPreSym = new Function(newname, Environment.getIntType(), new Type[0],
                    false, false, ASTLocatedElement.CREATED);
            try {
                env.addFunction(varAtPreSym);
                varAtPre = new Application(varAtPreSym, varAtPreSym.getResultType());
            } catch (EnvironmentException e) {
                throw new TermException(e);
            }
        }
    }
    
    private int insertProofObligations(int index) throws TermException {
        int sourceLineNumber = programTerm.getStatement().getSourceLineNumber();
        
        AssertStatement assertion = new AssertStatement(sourceLineNumber, invariant);
        programChanger.insertAt(index, assertion, "Continuation preserves invariant");
        index ++;
        
        if(variant != null) {
            Term varGt0 = tf.gte(variant, tf.number(0));
            Term varLtVar0 = tf.lt(variant, varAtPre);
            assertion = new AssertStatement(sourceLineNumber, tf.and(varGt0, varLtVar0));
            programChanger.insertAt(index, assertion, "Continuation reduces variant");
            index++;
        }
        
        programChanger.insertAt(index, new EndStatement(sourceLineNumber, Environment.getTrue()));
        index ++;
        
        return index;
    }

    private void insertAssumptions(int index) throws TermException {
        int sourceLineNumber = programTerm.getStatement().getSourceLineNumber();
        
        for (Function f : modifiedAssignables) {
            programChanger.insertAt(index, new HavocStatement(sourceLineNumber, tf.cons(f)));
            index ++;
        }
        
        if(variant != null) {
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
    @Nullable List<Function> assignedVars;
    
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
        successorIndices = new int[] { startIndex + 1 };
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