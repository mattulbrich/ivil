package de.uka.iti.pseudo.rule.where;

import java.util.Properties;

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

    public static final String CONST_NAME_PROPERTY = "skolemName";

    public NewSkolem() {
        super("newSkolem");
    }

    @Override 
    public boolean applyTo(WhereClause wc, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal, Environment env, 
            Properties properties) throws RuleException {

        // cast is safe ensured by tryApplyTo
        SchemaVariable argument = (SchemaVariable) wc.getArguments().get(0);
        String property = CONST_NAME_PROPERTY + "(" + argument.getName() + ")";
        
        try {
            Term instantiated = mc.instantiate(argument);
            if (instantiated instanceof SchemaVariable) {
                SchemaVariable schemaVar = (SchemaVariable) instantiated;
                Type type = schemaVar.getType();
                Type instantiatedType = mc.instantiateType(type);
                Function skolem = env.createNewSkolemConst(instantiatedType);
                mc.addInstantiation(schemaVar, new Application(skolem, instantiatedType));
                if(properties != null)
                    properties.put(property, skolem.getName());
            } else if(instantiated instanceof Application) {
                Application app = (Application) instantiated;
                String propertyStoredFunction = ruleApp.getWhereProperty(property);
                if(!app.getFunction().getName().equals(propertyStoredFunction)) {
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
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("newSkolem expects exactly 1 argument");
        if(!(arguments[0] instanceof SchemaVariable))
            throw new RuleException("newSkolem expects schema varible as first argument");
    }

    @Override 
    protected boolean applyTo(WhereClause whereClause,
            TermUnification mc) throws RuleException {
        throw new Error("cannot be invoked");
    }

    @Override 
    public boolean canApplyTo(WhereClause whereClause, TermUnification mc) throws RuleException {
        return true;
    }

}
