package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;

// TODO
public class AxiomCondition extends WhereCondition {

    public AxiomCondition() {
        super("axiom");
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env)
            throws RuleException {
        
        String axiomName = ruleApp.getProperties().get("axiomName");
        if(axiomName == null) {
            throw new RuleException("Property axiomName not set on rule application");
        }
        
        Axiom axiom = env.getAxiom(axiomName);
        if(axiom == null) {
            throw new RuleException("Axiom " + axiomName + " not defined in environment");
        }
        
        Term arg = actualArguments[0];
        if(!arg.equals(axiom.getTerm())) {
            // more detailed error message?
            throw new RuleException("Axiom " + axiomName + "("
                    + axiom.getTerm() + ") is not instantiated, but " + arg);
        }
        
        // rather throw an exception than return false in the other cases 
        return true;
        
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("axiom expects exactly 1 argument");
        Term arg = arguments[0];
        if(!Environment.getBoolType().equals(arg.getType()))
            throw new RuleException("axiom expects a boolean argument");
    }

}
