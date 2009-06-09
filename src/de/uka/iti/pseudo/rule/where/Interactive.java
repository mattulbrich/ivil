package de.uka.iti.pseudo.rule.where;

import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public class Interactive extends SimpleWhereCondition {

    public Interactive() {
        super("interact");
    }

    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("interact expects exactly 1 argument");
        if(!(arguments[0] instanceof SchemaVariable))
            throw new RuleException("interact expects schema varible as first argument");
    }

    protected boolean applyTo(Term[] arguments, TermUnification mc)
            throws RuleException {
        SchemaVariable sv = (SchemaVariable) arguments[0];
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

    @Override 
    public void verify(Term[] formalArguments,
            Term[] actualArguments, Properties properties) throws RuleException {
        // any instantiation is good enough for an interactive application
    }
    
}
