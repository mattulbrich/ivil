package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor.DepthTermVisitor;

public class TermUnification {
    
    private TypeUnification typeUnification = new TypeUnification();
    private Map<SchemaVariable, Term> instantiation = new HashMap<SchemaVariable, Term>();
    private Map<SchemaModality, Modality> modalityInstantiation = new HashMap<SchemaModality, Modality>();
    private TermInstantiator termInstantiator = new TermInstantiator(this);
    private TermMatcher termMatcher = new TermMatcher(this);
    
    private DepthTermVisitor schemaFinder = new DepthTermVisitor() { 
        public void visit(SchemaModality schemaModality) throws TermException {
            throw new TermException("Unexpected schema modality found: " + schemaModality);
        }
        
        public void visit(SchemaVariable schemaVariable) throws TermException {
            throw new TermException("Unexpected schema variable found: " + schemaVariable);
        }
    };
    
    public boolean leftUnify(Term adaptingTerm, Term fixTerm) {
        
        HashMap<SchemaVariable, Term> copyTermInst = new HashMap<SchemaVariable, Term>(instantiation);
        HashMap<SchemaModality, Modality> copyModInst = new HashMap<SchemaModality, Modality>(modalityInstantiation);

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
        
        // this is actually an assertion
        term.visit(schemaFinder);
        
        instantiation.put(sv, term);
    }
    
    public void addInstantiation(SchemaModality sm, Modality mod) throws TermException {

        // this is actually an assertion
        mod.visit(schemaFinder);
        
        modalityInstantiation.put(sm, mod);
    }
    
    public Term instantiate(SchemaVariable sv) {
        return instantiation.get(sv);
    }
    
    public Modality instantiate(SchemaModality sm) {
        return modalityInstantiation.get(sm);
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

}
