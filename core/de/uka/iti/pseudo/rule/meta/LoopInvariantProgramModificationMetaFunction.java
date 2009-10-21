package de.uka.iti.pseudo.rule.meta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProgramChanger;
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
    
    public LoopInvariantProgramModificationMetaFunction() {
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

    Set<Function> modifiedAssignables = new HashSet<Function>();
    Map<Function, Function> atPreSymbols = new HashMap<Function, Function>();
    private List<Function> otherAssignables;
    private Function varAtPre;
    
    private LiteralProgramTerm programTerm;
    private Term invariant;
    private Term variant;
    private Environment env;
    private TermFactory tf;
    private ProgramChanger programChanger;
    private Term atPreEqualities;
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

    Term apply() throws TermException, EnvironmentException {
        collectAssignables();
        
        otherAssignables = env.getAllAssignables();
        otherAssignables.removeAll(modifiedAssignables);

        Program program = programTerm.getProgram();
        int index = programTerm.getProgramIndex();
        // FIXME this wont work in general. Need environment to propose a name for me. 
        programChanger = new ProgramChanger(program, env);
        
        makeAtPreSymbols();
        makeAtPreEqualities();

        index = insertProofObligations(index);
        insertAssumptions(index);

        Program newProgram = programChanger.makeProgram(program.getName() + "'");
        env.addProgram(newProgram);
        
        LiteralProgramTerm newProgramTerm = new LiteralProgramTerm(index, programTerm.isTerminating(), newProgram);
        
        Term result = tf.impl(atPreEqualities, newProgramTerm);
        
        return result;
        
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
        
        assert size >= 1;
        
        int peek = stack.peek();
        
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
        
        if(hasLoop && analyser.assignedVar != null)
            modifiedAssignables.add(analyser.assignedVar);
        
        return hasLoop;
        
    }

    private void insertAssumptions(int index) throws TermException {
        int index0 = index;
        int sourceLineNumber = programTerm.getStatement().getSourceLineNumber();
        for (Function f : modifiedAssignables) {
            programChanger.insertAt(index, new HavocStatement(sourceLineNumber, tf.cons(f)));
            index ++;
        }
        
        programChanger.insertAt(index, new AssumeStatement(sourceLineNumber, invariant));
        index ++;
        
    }

    private void makeAtPreSymbols() throws TermException {
        for (Function f : otherAssignables) {
            String newname = env.createNewFunctionName(f.getName() + "AtPre");
            Function atpre = new Function(newname, f.getResultType(), new Type[0], 
                    false, false, ASTLocatedElement.BUILTIN);
            try {
                env.addFunction(atpre);
            } catch (EnvironmentException e) {
                throw new TermException(e);
            }
            atPreSymbols.put(f, atpre);
        }
        
        // XXX variant
        
        assert otherAssignables.size() == atPreSymbols.size(); 
    }

    private int insertProofObligations(int index) throws TermException {
        int sourceLineNumber = programTerm.getStatement().getSourceLineNumber();
        
        programChanger.insertAt(index, new AssertStatement(sourceLineNumber, invariant));
        index ++;
        
// XXX variant
//        if(!variant.equals(tf.number(0))) {
//            Term varGt0 = tf.gt(variant, tf.number(0));
//            Term varLtVar0 = tf.gt(variant, variant);
//            programChanger.insertAt(index, new AssertStatement(tf.and(varGt0, varLtVar0)));
//            index++;
//        }
        
        programChanger.insertAt(index, new AssertStatement(sourceLineNumber, atPreEqualities));
        index ++;
        
        programChanger.insertAt(index, new EndStatement(sourceLineNumber, Environment.getTrue()));
        index ++;
        
        return index;
    }

    private void makeAtPreEqualities() throws TermException {
        atPreEqualities = null;
        for (Function ass : otherAssignables) {
            Term eq = tf.eq(tf.cons(ass), tf.cons(atPreSymbols.get(ass)));
            
            if(atPreEqualities == null) {
                atPreEqualities = eq;
            } else {
                atPreEqualities = tf.and(atPreEqualities, eq);
            }
        }
        if(atPreEqualities == null)
            atPreEqualities = Environment.getTrue();
    }

}

class StatementAnalyser implements StatementVisitor {

    int startIndex;
    int[] successorIndices;
    Function assignedVar;
    
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
        assignedVar = ((Application)assignmentStatement.getTarget()).getFunction();
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
        assignedVar = ((Application)havocStatement.getTarget()).getFunction();
    }
    
}