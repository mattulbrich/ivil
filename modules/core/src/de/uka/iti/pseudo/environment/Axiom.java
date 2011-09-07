package de.uka.iti.pseudo.environment;

import java.util.Collection;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMatcher;

public class Axiom {

    /**
     * The name of this axiom
     */
    private String name;
    
    /**
     * The properties.
     */
    private Map<String, String> properties;
    
    /**
     * The axiom itself
     */
    private Term term;
    
    /**
     * The location.
     */
    private ASTLocatedElement location;

    public Axiom(@NonNull String name, @NonNull Term term,
            @NonNull Map<String, String> properties,
            @NonNull ASTLocatedElement location) throws EnvironmentException {
        super();
        this.name = name;
        this.properties = properties;
        this.term = term;
        this.location = location;
        
        if(!term.getType().equals(Environment.getBoolType()))
            throw new EnvironmentException("Axioms must have boolean type");
        
        if(TermMatcher.containsSchematic(term))
            throw new EnvironmentException("Axiom contains schema entity");
    }

    /**
     * gets a property. Properties are specified using the "tag" keyword in
     * environments. If the property is not set, null is returned. If the
     * property has been defined without a value, an empty string "" is returned
     * 
     * <p>Please use a constant defined in {@link RuleTagConstants} as argument
     * to keep all sensible tags at one place.
     * 
     * @see RuleTagConstants
     * 
     * @param string
     *            name of the property to retrieve
     * @return the property if it is defined, null otherwise
     */
    public @Nullable String getProperty(String string) {
        return properties.get(string);
    }
    
    /**
     * Gets a collection which contains the names of all defined properties for this
     * rule. The entries to this collections are different from null and can be used
     * as keys to {@link #getProperty(String)}.
     *  
     * @return an unmodifiable collection of strings.
     */
    public Collection<String> getDefinedProperties() {
        return properties.keySet();
    }
    

    /**
     * Gets the name of this axiom.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    public String toString() {
        return "Axiom[" + name + "]";
    }

    public ASTLocatedElement getDeclaration() {
        return location;
    }

    public Term getTerm() {
        return term;
    }

    public void dump() {
        System.out.println("  Axiom " + name);
        System.out.println("        " + term);
    }
}
