package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgram;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

// TODO DOC
public abstract class DefaultTermVisitor implements TermVisitor {
    
    public static class DepthTermVisitor extends DefaultTermVisitor {

        protected void defaultVisitTerm(Term term) throws TermException {
            for (Term t : term.getSubterms()) {
                t.visit(this);
            }
        }

        public void visit(UpdateTerm updateTerm) throws TermException {
            defaultVisitTerm(updateTerm);
            for(AssignmentStatement ass : updateTerm.getAssignments()) {
                ass.getValue().visit(this);
            }
        }

    }

    protected abstract void defaultVisitTerm(Term term) throws TermException;
    
    public void visit(Variable variable) throws TermException {
        defaultVisitTerm(variable);
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

    public void visit(SchemaProgram schemaProgramTerm) throws TermException {
        defaultVisitTerm(schemaProgramTerm);
    }

    public void visit(LiteralProgramTerm literalProgramTerm) throws TermException {
        defaultVisitTerm(literalProgramTerm);
    }

    public void visit(UpdateTerm updateTerm) throws TermException {
        defaultVisitTerm(updateTerm);
    }
    
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        defaultVisitTerm(schemaUpdateTerm);
    }

}
