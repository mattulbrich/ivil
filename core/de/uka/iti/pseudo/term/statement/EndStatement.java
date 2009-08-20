package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class EndStatement extends Statement {

    public EndStatement(int sourceLineNumber, Term conditionTerm) throws TermException {
        super(sourceLineNumber, conditionTerm);
        ensureCondition();
    }

    public String toString(boolean typed) {
        return super.toString(typed) + "end "
                + getSubterms().get(0).toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }


}
