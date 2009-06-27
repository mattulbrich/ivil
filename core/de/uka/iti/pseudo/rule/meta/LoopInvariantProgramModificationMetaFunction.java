package de.uka.iti.pseudo.rule.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProgramChanger;
import de.uka.iti.pseudo.environment.SourceAnnotation;
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
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.HavocStatement;

public class LoopInvariantProgramModificationMetaFunction extends MetaFunction {
    
    public LoopInvariantProgramModificationMetaFunction() {
        super(BOOL, "$$loopInvPrgMod", BOOL, BOOL, INT, TypeVariable.ALPHA);
    }

    private static final Type BOOL = Environment.getBoolType();
    private static final Type INT= Environment.getIntType();
    
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
        Term modifies = application.getSubterm(3);
        
        // use an external object so that no state is stored in the meta
        // function
        LoopModifier modifier = new LoopModifier(programTerm, invariant, variant, modifies, env);
        
        try {
            return modifier.apply();
        } catch (EnvironmentException e) {
            throw new TermException(e);
        }
    }
}

        
class LoopModifier {

    List<Function> modifiedAssignables = new LinkedList<Function>();
    Map<Function, Function> atPreSymbols = new HashMap<Function, Function>();
    private List<Function> otherAssignables;
    private Function varAtPre;
    
    private LiteralProgramTerm programTerm;
    private Term invariant;
    private Term variant;
    private Term modifies;
    private Environment env;
    private TermFactory tf;
    private ProgramChanger programChanger;
    private Term atPreEqualities;

    public LoopModifier(LiteralProgramTerm programTerm, Term invariant,
            Term variant, Term modifies, Environment env) {
        this.programTerm = programTerm;
        this.invariant = invariant;
        this.variant = variant;
        this.modifies = modifies;
        this.env = env;
        this.tf = new TermFactory(env);
    }

    Term apply() throws TermException, EnvironmentException {
        collectAssignables(modifies, modifiedAssignables);
        
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

    private void insertAssumptions(int index) throws TermException {
        int index0 = index;
        for (Function f : modifiedAssignables) {
            programChanger.insertAt(index, new HavocStatement(tf.cons(f)));
            index ++;
        }
        
        programChanger.insertAt(index, new AssumeStatement(invariant));
        index ++;
        
        programChanger.addSourceAnnotation(new SourceAnnotation("loop STARTS with invariant", index0));
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
        programChanger.insertAt(index, new AssertStatement(invariant));
        programChanger.addSourceAnnotation(new SourceAnnotation("loop PRESERVES invariants", index));
        index ++;
        
// XXX variant
//        if(!variant.equals(tf.number(0))) {
//            Term varGt0 = tf.gt(variant, tf.number(0));
//            Term varLtVar0 = tf.gt(variant, variant);
//            programChanger.insertAt(index, new AssertStatement(tf.and(varGt0, varLtVar0)));
//            index++;
//        }
        
        programChanger.insertAt(index, new AssertStatement(atPreEqualities));
        index ++;
        
        programChanger.insertAt(index, new EndStatement(Environment.getTrue()));
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

    private void collectAssignables(Term modifies, List<Function> modifiedAssignables) throws TermException {
        if (modifies instanceof Application) {
            Application appl = (Application) modifies;
            Function f = appl.getFunction();
            if(f.getName().equals("$enumerateAssignables")) {
                collectAssignables(appl.getSubterm(0), modifiedAssignables);
                collectAssignables(appl.getSubterm(1), modifiedAssignables);
            } else if(f.isAssignable()) {
                modifiedAssignables.add(f);
            } else {
                throw new TermException("Only assignables may be enumerated in modifies clause: " + appl);
            }
        }
    }

}
