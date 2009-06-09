package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaModality;
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
    private Map<String, Modality> modalityInstantiation = new HashMap<String, Modality>();
    private TermInstantiator termInstantiator = new TermInstantiator(this);
    private TermMatcher termMatcher = new TermMatcher(this);
    
    private boolean containsSchema = false;
    
    public boolean leftUnify(Term adaptingTerm, Term fixTerm) {
        
        HashMap<String, Term> copyTermInst = new HashMap<String, Term>(instantiation);
        HashMap<String, Modality> copyModInst = new HashMap<String, Modality>(modalityInstantiation);

        try {
            
            termMatcher.compare(adaptingTerm, fixTerm);
            return true;
            
        } catch (TermException e) {
            instantiation = copyTermInst;
            modalityInstantiation = copyModInst;
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
            for (String s : modalityInstantiation.keySet()) {
                modalityInstantiation.put(s, instantiate(modalityInstantiation.get(s)));
            }
        }
        
        containsSchema |= !scv.isEmpty();
    }
    
    public void addInstantiation(SchemaModality sm, Modality mod) throws TermException {

        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        scv.collect(mod);
        
        if(scv.getSchemaModalities().contains(sm)) {
            throw new UnificationException("The schema modalitycannot be instantiated, occur check failed", sm, mod);
        }
        
        assert modalityInstantiation.get(sm) == null;
        
        modalityInstantiation.put(sm.getName(), mod);
        
        // TODO see above
        if(containsSchema) {
            for (String s : instantiation.keySet()) {
                instantiation.put(s, instantiate(instantiation.get(s)));
            }
            for (String s : modalityInstantiation.keySet()) {
                modalityInstantiation.put(s, instantiate(modalityInstantiation.get(s)));
            }
        }
        
        containsSchema |= !scv.isEmpty();
    }
    
    public Term getTermFor(SchemaVariable sv) {
        return instantiation.get(sv.getName());
    }
    
    public Modality getModalityFor(SchemaModality sm) {
        return modalityInstantiation.get(sm.getName());
    }

//    public void match(Term term1, Term term2) {
//        // TODO Auto-generated method stub
//    }

    public Term instantiate(Term toInst) throws TermException {
        return termInstantiator.instantiate(toInst);
    }
    
    private Modality instantiate(Modality toInst) throws TermException {
        return termInstantiator.instantiate(toInst);
    }
    
    public TermUnification clone() {
        TermUnification retval = new TermUnification();
        retval.instantiation.putAll(instantiation);
        retval.modalityInstantiation.putAll(modalityInstantiation);
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
        public void visit(SchemaModality schemaModality) throws TermException {
            throw new TermException("Unexpected schema modality found: " + schemaModality);
        }
        
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
    
    public static boolean containsSchemaIdentifier(Modality m) {
        try {
            m.visit(schemaFinder);
            return false;
        } catch (TermException e) {
            return true;
        }
    }

    public Map<String, Term> getTermInstantiation() {
        return new HashMap<String, Term>(instantiation);
    }

    public Map<String, Modality> getModalityInstantiation() {
        return new HashMap<String, Modality>(modalityInstantiation);
    }



}
