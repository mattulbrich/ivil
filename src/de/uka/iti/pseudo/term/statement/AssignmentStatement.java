package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class AssignmentStatement extends Statement {

    private Term target;
    private Term value;

    public AssignmentStatement(Term target, Term value) throws TermException {
        this.target = target;
        this.value = value;;
        check();
    }

    private void check() throws TermException {
        if (target instanceof Application) {
            Application appl = (Application) target;
            Function func = appl.getFunction();
            if(!func.isAssignable())
                throw new TermException("Target in an assignment needs to be 'assignable'");
        }
    }

    public String toString(boolean typed) {
        return target.toString(typed) + " := " + value.toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public Term getTarget() {
        return target;
    }

    public Term getValue() {
        return value;
    }


}
