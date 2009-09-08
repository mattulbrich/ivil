package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * AssignmentStatements act as statements in programs, but they can also appear
 * as basic assignments in updates.
 */
public class AssignmentStatement extends Statement {

    public AssignmentStatement(int sourceLineNumber, Term target, Term value) throws TermException {
        super(sourceLineNumber, new Term[] { target, value });
        check();
    }

    public AssignmentStatement(Term target, Term value) throws TermException {
        this(-1, target, value);
    }

    private void check() throws TermException {
        if (getTarget() instanceof Application) {
            Application appl = (Application) getTarget();
            Function func = appl.getFunction();
            if(!func.isAssignable())
                throw new TermException("Target in an assignment needs to be 'assignable'");
        }
    }

    public String toString(boolean typed) {
        return getTarget().toString(false) + " := " + getValue().toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public Term getTarget() {
        return getSubterms().get(0);
    }

    public Term getValue() {
        return getSubterms().get(1);
    }

}
