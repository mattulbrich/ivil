package de.uka.iti.pseudo.rule.where;

import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.ImmutableRuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO DOC l8er
/*
 * Format 
 *    notFreeIn { variable } { term }
 */
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

    @Override 
    public boolean applyTo(WhereClause wc, TermUnification mc) throws RuleException {

        // get schema variable
        // instantiate
        // make sure it is a variable
        // make sure collect free vars does not find it.
        
        return true;
    }

}
