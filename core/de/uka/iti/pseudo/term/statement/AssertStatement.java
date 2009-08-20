package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;


public class AssertStatement extends Statement {

    public AssertStatement(int sourceLineNumber, Term conditionTerm) throws TermException {
        super(sourceLineNumber, conditionTerm);
        ensureCondition();
    }

    public String toString(boolean typed) {
        return super.toString(typed) + 
            "assert " + getSubterms().get(0).toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }


}
