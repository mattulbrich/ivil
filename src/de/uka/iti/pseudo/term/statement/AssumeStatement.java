package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class AssumeStatement extends Statement {

    public AssumeStatement(Term conditionTerm) throws TermException {
        super(conditionTerm);
        ensureCondition();
    }

    public String toString(boolean typed) {
        return "assume " + getSubterms().get(0).toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    
}
