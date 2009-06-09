package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public class NewSkolem extends WhereCondition {

    public static final String CONST_NAME_PROPERTY = "constName";

    public NewSkolem() {
        super("newSkolem");
    }

    @Override public boolean applyTo(WhereClause wc, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal, Environment env) throws RuleException {
        
        Term argument = wc.getArguments().get(0);
        
        assert argument instanceof SchemaVariable;
        
        try {
            Term instantiated = mc.instantiate(argument);
            if (instantiated instanceof SchemaVariable) {
                SchemaVariable schemaVar = (SchemaVariable) instantiated;
                Type type = schemaVar.getType();
                Function skolem = env.createNewSkolemConst(type);
                mc.addInstantiation(schemaVar, new Application(skolem, type));
                wc.getProperties().put(CONST_NAME_PROPERTY, skolem.getName());
            } else if(instantiated instanceof Application) {
                Application app = (Application) instantiated;
                if(!app.getFunction().getName().equals(wc.getProperties().get(CONST_NAME_PROPERTY))) {
                    throw new RuleException("Unexpected skolem instantiation: " + app );
                }
            } else {
                throw new RuleException("Unexpected skolem instantiation: " + instantiated );
            }
            
        } catch (TermException e) {
            throw new RuleException("During creation of new skolem", e);
        }
        
        return true;
    }

    @Override 
    public void tryToApplyTo(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("newSkolem expects exactly 1 argument");
        if(!(arguments[0] instanceof SchemaVariable))
            throw new RuleException("newSkolem expects schema varible as first argument");
    }

}
