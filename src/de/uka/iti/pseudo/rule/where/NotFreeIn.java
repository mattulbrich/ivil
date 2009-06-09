package de.uka.iti.pseudo.rule.where;

import java.util.Map;

import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO DOC l8er
/*
 * Format 
 *    notFreeIn { variable } { term }
 */
public class NotFreeIn extends SimpleWhereCondition {

    public NotFreeIn() {
        super("notFreeIn");
    }

    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 2)
            throw new RuleException("notFreeIn expects exactly 2 arguments");
        if(arguments[0] instanceof SchemaVariable)
            throw new RuleException("notFreeIn expects schema varible as first argument");
    }
    
    protected boolean verify(Term[] arguments) {
        // TODO Implement NotFreeIn verify
        // get schema variable
        // instantiate
        // make sure it is a variable
        // make sure collect free vars does not find it.
        // throw exception if there is still a schema variable.
        
        return true;
    }

    @Override 
    public boolean applyTo(Term[] arguments, TermUnification mc) throws RuleException {
        try {
            Term instantiated[] = new Term[arguments.length];
            for (int i = 0; i < instantiated.length; i++) {
                instantiated[i] = mc.instantiate(arguments[i]);
            }
            return verify(instantiated);
        } catch (TermException e) {
            throw new RuleException("Exception during instantiation", e);
        }
    }

    @Override 
    public void verify(Term[] formalArguments,
            Term[] actualArguments, Map<String, String> properties) throws RuleException {
        if(!verify(actualArguments)) {
            throw new RuleException("Variable " + actualArguments[0] + 
                    " is not free in " + actualArguments[1]);
        }
    }

}
