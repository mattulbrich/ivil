package de.uka.iti.pseudo.rule.where;

import java.util.Properties;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public class NewSkolem extends WhereCondition {

    public static final String CONST_NAME_PROPERTY = "skolemName";
    public static final String CONST_TYPE_PROPERTY = "skolemType";

    public NewSkolem() {
        super("newSkolem");
    }

    
    @Override 
    public boolean applyTo(Term[] arguments, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal, Environment env,
            Properties properties, boolean commit) throws RuleException {
        

        // cast is safe ensured by checkSyntax
        SchemaVariable argument = (SchemaVariable) arguments[0];
        String nameProperty = CONST_NAME_PROPERTY + "(" + argument.getName() + ")";
        String typeProperty = CONST_TYPE_PROPERTY + "(" + argument.getName() + ")";
        
        try {
            Term instantiation = mc.getTermFor(argument);
            if (instantiation != null) 
                throw new RuleException("SchemaVariable " + argument +
                        " already instantiated: " + instantiation);
            
            if(!commit)
                return true;
            
            Type type = argument.getType();
            Function skolem = env.createNewSkolemConst(type);
            mc.addInstantiation(argument, new Application(skolem, type));
            
            if(properties != null) {
                properties.put(nameProperty, skolem.getName());
                properties.put(typeProperty, skolem.getResultType().toString());
            }
            
            return true;
            
        } catch (TermException e) {
            throw new RuleException("During creation of new skolem", e);
        }
        
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
            Term[] actualArguments, Properties properties) throws RuleException {

        // works thanks to checkSyntax
        Term argument = actualArguments[0];
        SchemaVariable formal = (SchemaVariable) formalArguments[0];

        String nameProperty = CONST_NAME_PROPERTY + "(" + formal.getName()  + ")";

        if (argument instanceof Application) {
            Application app = (Application) argument;
            String propertyStoredFunction = properties.getProperty(nameProperty);
            if (!app.getFunction().getName().equals(propertyStoredFunction)) {
                throw new RuleException("Unexpected skolem instantiation: " + app);
            }
        } else {
            throw new RuleException("Unexpected skolem instantiation: " + argument);
        }
    }
    
    @Override 
    public void wasImported(Term[] formalArguments, Environment env, Properties properties) 
            throws RuleException {
        
        SchemaVariable schema = (SchemaVariable) formalArguments[0];
        
        String nameProperty = CONST_NAME_PROPERTY + "(" + schema.getName() + ")";
        String typeProperty = CONST_TYPE_PROPERTY + "(" + schema.getName() + ")";
        
        String name = properties.getProperty(nameProperty);
        String typeString = properties.getProperty(typeProperty);
        
        if(name == null) {
            throw new RuleException("Property " + nameProperty + " not set as necessary");
        }

        if(typeString == null) {
            throw new RuleException("Property " + typeProperty + " not set as necessary");
        }

        Type type;
        try {
            type = TermMaker.makeType(typeString, env);
        } catch (TermException e) {
            throw new RuleException("Cannot parse type of skolem constant: " + typeString, e);
        }
     
        Function skolem = new Function(name, type, new Type[0], false, false, ASTLocatedElement.BUILTIN);
        try {
            env.addFunction(skolem);
        } catch (EnvironmentException e) {
            throw new RuleException("Cannot add skolem constant to environment", e);
        }
        
    }

}
