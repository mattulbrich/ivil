package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.MatchingContext;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;

public class NotFreeIn extends WhereCondition {

    public NotFreeIn() {
        super("notFreeIn");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void tryToApplyTo(Term[] arguments) throws RuleException {
        if(arguments.length != 2)
            throw new RuleException("notFreeIn expects exactly 2 arguments");
        if(arguments[0] instanceof SchemaVariable)
            throw new RuleException("notFreeIn expects schema varible as first argument");
    }

    @Override public boolean applyTo(Term[] arguments, MatchingContext mc,
            RuleApplication ruleApp, ProofNode goal) throws RuleException {

        // get schema variable
        // instantiate
        // make sure it is a variable
        // make sure collect free vars does not find it.
        
        return true;
    }

}
