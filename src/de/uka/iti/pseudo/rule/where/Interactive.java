package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;

// TODO Documentation needed
public class Interactive extends WhereCondition {

    public static final String INTERACTION = "interact";

    public Interactive() {
        super("interact");
    }

    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("interact expects exactly 1 argument");
        if(!(arguments[0] instanceof SchemaVariable))
            throw new RuleException("interact expects schema varible as first argument");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        if(ruleApp.hasMutableProperties()) {
            SchemaVariable sv = (SchemaVariable) formalArguments[0];
            ruleApp.getProperties().put(INTERACTION + "(" + sv.getName() + ")", 
                    sv.getType().toString());
        }
        return true;
    }
    
}
