package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor.DepthTermVisitor;
// TODO DOC DOC
public class TermUnification {
    
    private TypeUnification typeUnification = new TypeUnification();
    private Map<String, Term> instantiation = new HashMap<String, Term>();
    private Map<String, Modality> modalityInstantiation = new HashMap<String, Modality>();
    private TermInstantiator termInstantiator = new TermInstantiator(this);
    private TermMatcher termMatcher = new TermMatcher(this);
    
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
    
    /*package*/ void addInstantiation(SchemaVariable sv, Term term) throws TermException {
        
        assert !containsSchemaIdentifier(term) : term;
        assert instantiation.get(sv) == null;
        
        instantiation.put(sv.getName(), term);
    }
    
    /*package*/ void addInstantiation(SchemaModality sm, Modality mod) throws TermException {

        assert !containsSchemaIdentifier(mod) : mod;
        assert modalityInstantiation.get(sm) == null;
        
        modalityInstantiation.put(sm.getName(), mod);
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
    
    private static final DepthTermVisitor schemaFinder = new DepthTermVisitor() { 
        public void visit(SchemaModality schemaModality) throws TermException {
            throw new TermException("Unexpected schema modality found: " + schemaModality);
        }
        
        public void visit(SchemaVariable schemaVariable) throws TermException {
            throw new TermException("Unexpected schema variable found: " + schemaVariable);
        }
    };
    
    public boolean containsSchemaIdentifier(Term t) {
        try {
            t.visit(schemaFinder);
            return false;
        } catch (TermException e) {
            return true;
        }
    }
    
    public boolean containsSchemaIdentifier(Modality m) {
        try {
            m.visit(schemaFinder);
            return false;
        } catch (TermException e) {
            return true;
        }
    }



}
