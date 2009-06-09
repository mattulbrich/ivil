package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.AssignModality;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.IfModality;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.WhileModality;
// TODO DOC
// for left unification only
public class TermMatcher extends DefaultTermVisitor {

    private TermUnification termUnification;
    private Term compareTerm;
    private Modality compareModality;
    
    public TermMatcher(TermUnification termUnification) {
        this.termUnification = termUnification;
    }
    
    public void compare(Term t1, Term t2) throws TermException {
        termUnification.getTypeUnification().leftUnify(t1.getType(), t2.getType());
        if (t1 instanceof SchemaVariable) {
            SchemaVariable sv = (SchemaVariable) t1;
            Term inst = termUnification.getTermFor(sv);
            if(inst != null) {
                compare(inst, t2);
            } else {
                termUnification.addInstantiation(sv, t2);
                termUnification.getTypeUnification().leftUnify(new TypeVariable(sv.getName()), t2.getType());
            }
        } else if(t1.getClass() == t2.getClass()) {
            compareTerm = t2;
            t1.visit(this);
        } else 
            throw new UnificationException("Incomparable types of terms", t1, t2);
    }
    
    private void compare(Modality m1, Modality m2) throws TermException {
        if (m1 instanceof SchemaModality) {
            SchemaModality sm = (SchemaModality) m1;
            Modality inst = termUnification.getModalityFor(sm);
            if(inst != null) {
                compare(inst, m2);
            } else {
                termUnification.addInstantiation(sm, m2);
            }
        } else if(m1.getClass() == m2.getClass()) {
            compareModality = m2;
            m1.visit(this);
        } else 
            throw new UnificationException("Incomparable types of modalities", m1, m2);
    }

    
    @Override 
    protected void defaultVisitModality(Modality modality)    throws TermException {
        List<Modality> sub1 = modality.getSubModalities();
        List<Modality> sub2 = compareModality.getSubModalities();
        
        for (int i = 0; i < sub1.size(); i++) {
            compare(sub1.get(i), sub2.get(i));
        }
    }

    @Override
    protected void defaultVisitTerm(Term term) throws TermException {
        List<Term> sub1 = term.getSubterms();
        List<Term> sub2 = compareTerm.getSubterms();
        
        for (int i = 0; i < sub1.size(); i++) {
            compare(sub1.get(i), sub2.get(i));
        }
    }
    
    public void visit(Variable v1) throws TermException {
        Variable v2 = (Variable) compareTerm;
        
        if(!v1.getName().equals(v2.getName()))
            throw new UnificationException(v1, v2);
        
        termUnification.getTypeUnification().leftUnify(v1.getType(), v2.getType());
    }


    @Override 
    public void visit(Binding b1) throws TermException {
        Binding b2 = (Binding) compareTerm;
        
        // binders are identical, not only equal
        if(b1.getBinder() != b2.getBinder())
            throw new UnificationException(b1, b2);
        
        termUnification.getTypeUnification().leftUnify(b1.getVariableType(), b2.getVariableType());
        
        if(b1.hasSchemaVariable()) {
            // rhs may not contain schema stuff
            assert !b2.hasSchemaVariable();
            termUnification.addInstantiation(
                    new SchemaVariable(b1.getVariableName(), b1.getVariableType()), 
                    new Variable(b2.getVariableName(), b2.getVariableType()));
        } else if(!b1.getVariableName().equals(b2.getVariableName())) {
            throw new UnificationException("Different variable", b1, b2);
        }
        
        defaultVisitTerm(b1);
    }

    @Override 
    public void visit(Application a1) throws TermException {
        Application a2 = (Application) compareTerm;
        
        // there is only one function symbols, check via ==
        if(a1.getFunction() != a2.getFunction())
            throw new UnificationException(a1, a2);
        
        termUnification.getTypeUnification().leftUnify(a1.getType(), a2.getType());
        
        defaultVisitTerm(a1);
    }

    @Override 
    public void visit(SchemaVariable schemaVariable) throws TermException {
        throw new Error("cannot be called");
    }
    
    @Override
    public void visit(ModalityTerm m1) throws TermException {
        ModalityTerm m2 = (ModalityTerm) compareTerm;
        compare(m1.getModality(), m2.getModality());
        compare(m1.getSubterm(), m2.getSubterm());
    }


    @Override
    public void visit(AssignModality am1) throws TermException {
        AssignModality am2 = (AssignModality) compareModality;
        
        if(am1.isSchemaAssignment()) {
            // this is left unification: there are no schemas on rhs.
            assert !am2.isSchemaAssignment();
            
            SchemaVariable schemaVar = (SchemaVariable) am1.getAssignTarget();
            Function function = (Function) am2.getAssignTarget();
            assert function.isAssignable();
            
            // there are no free type vars, so this is ok
            Application replaceBy = new Application(function, function.getResultType());
            
            termUnification.getTypeUnification().leftUnify(schemaVar.getType(), function.getResultType());
            termUnification.addInstantiation(schemaVar, replaceBy);
        } else {
            if(!am1.getAssignTarget().equals(am2.getAssignTarget()))
                throw new UnificationException("different assigned consts", am1, am2);
        }
        
        compare(am1.getAssignedTerm(), am2.getAssignedTerm());
    }

    @Override public void visit(IfModality ifModality) throws TermException {
        IfModality ifModality2 = (IfModality)compareModality;
        
        if(ifModality.countModalities() != ifModality2.countModalities())
            throw new UnificationException("One if has else, one has not", ifModality, ifModality2);
        
        defaultVisitModality(ifModality);
        
        compare(ifModality.getConditionTerm(), 
                ifModality2.getConditionTerm());
    }

    // Nothing to be done for skip
    // Nothing to be done for Compound

    @Override public void visit(WhileModality whileModality)
            throws TermException {
        
        WhileModality whileModality2 = (WhileModality)compareModality;
        
        defaultVisitModality(whileModality);
        
        compare(whileModality.getConditionTerm(), 
                whileModality2.getConditionTerm());
    }

    @Override public void visit(SchemaModality schemaModality)
            throws TermException {

            throw new Error("cannot be called");

    }

}
