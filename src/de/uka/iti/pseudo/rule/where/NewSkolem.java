package de.uka.iti.pseudo.rule.where;

import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public class NewSkolem extends WhereCondition {

    public static final String CONST_NAME_PROPERTY = "skolemName";

    public NewSkolem() {
        super("newSkolem");
    }

    
    @Override 
    public boolean applyTo(Term[] arguments, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal, Environment env,
            Map<String, String> properties, boolean commit) throws RuleException {
        

        // cast is safe ensured by checkSyntax
        SchemaVariable argument = (SchemaVariable) arguments[0];
        String nameProperty = CONST_NAME_PROPERTY + "(" + argument.getName() + ")";
        
        Term instantiation = mc.getTermFor(argument);
        if (instantiation != null) 
            throw new RuleException("SchemaVariable " + argument +
                    " already instantiated: " + instantiation);
            
        String skolemName = env.createNewFunctionName(argument.getName().substring(1));
            
        if(properties != null) {
            properties.put(nameProperty, skolemName);
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
    public void verify(Term[] formalArguments,
            Term[] actualArguments, Map<String, String> properties) throws RuleException {

        // works thanks to checkSyntax
        Term argument = actualArguments[0];
        SchemaVariable formal = (SchemaVariable) formalArguments[0];

        String nameProperty = CONST_NAME_PROPERTY + "(" + formal.getName()  + ")";

        if (argument instanceof Application) {
            Application app = (Application) argument;
            String propertyStoredFunction = properties.get(nameProperty);
            if (!app.getFunction().getName().equals(propertyStoredFunction)) {
                throw new RuleException("Unexpected skolem instantiation: " + app);
            }
        } else {
            throw new RuleException("Unexpected skolem instantiation: " + argument);
        }
    }
    
}
