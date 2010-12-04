package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ast.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermFactory;

public final class TranslationPhase {

    /**
     * Name used in ivil; this is necessary, as ivil has only one global
     * namespace.
     */
    public final Map<Variable, String> variableNames = new HashMap<Variable, String>();
    public final Map<Expression, Term> terms = new HashMap<Expression, Term>();

    private Term problem = null;

    public void create(EnvironmentCreationState state) throws EnvironmentCreationException {

        // fill environment with programs
        new ProgramMaker(state);

        // create a problem
        {
            for (ProcedureDeclaration decl : state.names.procedureSpace.values()) {
                if (decl.getBody() != null) {
                    try {
                        if (problem == null) {

                            problem = new LiteralProgramTerm(0, false, state.env.getProgram(decl.getName()));
                        } else {
                            Term[] args = new Term[2];
                            args[0] = problem;
                            args[1] = new LiteralProgramTerm(0, false, state.env.getProgram(decl.getName()));

                            problem = new Application(state.env.getFunction("$and"), Environment.getBoolType(), args);
                        }
                    } catch (TermException e) {
                        e.printStackTrace();
                    }
                }
            }
            for(ASTElement e : state.root.getChildren()){
                if(e instanceof ProcedureImplementation){
                    ProcedureImplementation decl = (ProcedureImplementation)e;                   
                    try {
                        Term[] args = new Term[2];
                        args[0] = problem;
                        args[1] = new LiteralProgramTerm(0, false, state.env.getProgram(decl.getName()));

                        problem = new Application(state.env.getFunction("$and"), Environment.getBoolType(), args);
                            
                    } catch (TermException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        // fix the environment?
    }

    final Term getProblem() {
        return problem;
    }
}
