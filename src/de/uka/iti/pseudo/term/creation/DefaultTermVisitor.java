package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.AssignModality;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.CompoundModality;
import de.uka.iti.pseudo.term.IfModality;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.ModalityVisitor;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.SkipModality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.WhileModality;

public abstract class DefaultTermVisitor implements ModalityVisitor, TermVisitor {
    
    public static class DepthTermVisitor extends DefaultTermVisitor {
        protected void defaultVisitModality(Modality modality) throws TermException {
            for (Modality mod : modality.getSubModalities()) {
                mod.visit(this);
            }
        }

        protected void defaultVisitTerm(Term term) throws TermException {
            for (Term t : term.getSubterms()) {
                t.visit(this);
            }
        }
    }

    protected abstract void defaultVisitTerm(Term term) throws TermException;
    protected abstract void defaultVisitModality(Modality modality) throws TermException;
    
    public void visit(AssignModality assignModality) throws TermException {
        defaultVisitModality(assignModality);
    }

    public void visit(CompoundModality compoundModality) throws TermException {
        defaultVisitModality(compoundModality);
    }

    public void visit(IfModality ifModality) throws TermException {
        defaultVisitModality(ifModality);
    }

    public void visit(SkipModality skipModality) throws TermException {
        defaultVisitModality(skipModality);
    }
    
    public void visit(SchemaModality schemaModality) throws TermException {
        defaultVisitModality(schemaModality);
    }

    public void visit(WhileModality whileModality) throws TermException {
        defaultVisitModality(whileModality);
    }

    public void visit(Variable variable) throws TermException {
        defaultVisitTerm(variable);
    }

    public void visit(ModalityTerm modalityTerm) throws TermException {
        defaultVisitTerm(modalityTerm);
    }

    public void visit(Binding binding) throws TermException {
        defaultVisitTerm(binding);
    }

    public void visit(Application application) throws TermException {
        defaultVisitTerm(application);
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        defaultVisitTerm(schemaVariable);
    }

}
