package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public class Interactive extends WhereCondition {

    public Interactive() {
        super("interact");
    }

    protected boolean applyTo(WhereClause whereClause, TermUnification mc) throws RuleException {
        return true;
    }

    public boolean canApplyTo(WhereClause whereClause, TermUnification mc) throws RuleException {
        SchemaVariable sv = (SchemaVariable) whereClause.getArguments().get(0);
        Term subst = mc.getTermFor(sv);
        if(subst == null) {
            try {
                mc.addInstantiation(sv, new Application(Environment.getInteractionSymbol(), 
                        mc.instantiateType(sv.getType())));
            } catch (TermException e) {
                throw new RuleException(e);
            }
        }
        
        return true;
    }

    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("interact expects exactly 1 argument");
        if(!(arguments[0] instanceof SchemaVariable))
            throw new RuleException("interact expects schema varible as first argument");
    }
    
}
