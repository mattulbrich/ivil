package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.rule.RuleException;
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
        // if(arguments[0] instance of SchemaIdentifier)
    }

}
