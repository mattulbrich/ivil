package de.uka.iti.pseudo.term.creation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor.DepthTermVisitor;
// TODO DOC DOC
public class TermUnification {
    
    private TypeUnification typeUnification = new TypeUnification();
    private Map<String, Term> instantiation = new HashMap<String, Term>();
    private TermInstantiator termInstantiator = new TermInstantiator(this);
    private TermMatcher termMatcher;
    private Environment env;
    
    private boolean containsSchema = false;
    
    public TermUnification(Environment env) {
        termMatcher = new TermMatcher(this, env);
        this.env = env;
    }
    
    public boolean leftUnify(Term adaptingTerm, Term fixTerm) {
        
        HashMap<String, Term> copyTermInst = new HashMap<String, Term>(instantiation);

        try {
            
            termMatcher.compare(adaptingTerm, fixTerm);
            return true;
            
        } catch (TermException e) {
            instantiation = copyTermInst;
            return false;
        }
        
    }
    
    public void addInstantiation(SchemaVariable sv, Term term) throws TermException {
        
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        scv.collect(term);
        
        if(scv.getSchemaVariables().contains(sv)) {
            throw new UnificationException("The schema variable cannot be instantiated, occur check failed", sv, term);
        }
        
        assert instantiation.get(sv) == null;
        
        instantiation.put(sv.getName(), term);
        
        if(containsSchema) {
            for (String s : instantiation.keySet()) {
                instantiation.put(s, instantiate(instantiation.get(s)));
            }
        }
        
        containsSchema |= !scv.isEmpty();
    }
    
    public Term getTermFor(SchemaVariable sv) {
        return instantiation.get(sv.getName());
    }
    
    public Term instantiate(Term toInst) throws TermException {
        return termInstantiator.instantiate(toInst);
    }
    
    public TermUnification clone() {
        TermUnification retval = new TermUnification(env);
        retval.instantiation.putAll(instantiation);
        retval.typeUnification = typeUnification.clone();
        return retval;
    }

    public Type instantiateType(Type type) {
        return typeUnification.instantiate(type);
    }
    
    public TypeUnification getTypeUnification() {
        return typeUnification;
    }
    
    /**
     * @see SchemaCollectorVisitor
     */
    private static final DepthTermVisitor schemaFinder = new DepthTermVisitor() { 
        public void visit(SchemaVariable schemaVariable) throws TermException {
            throw new TermException("Unexpected schema variable found: " + schemaVariable);
        }
    };
    
    public static boolean containsSchemaIdentifier(Term t) {
        try {
            t.visit(schemaFinder);
            return false;
        } catch (TermException e) {
            return true;
        }
    }
    
    public Map<String, Term> getTermInstantiation() {
        return Collections.unmodifiableMap(instantiation);
    }

    public TermInstantiator getTermInstantiator() {
        return termInstantiator;
    }

}
