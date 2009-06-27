package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.TermException;


public class SkipStatement extends Statement {

    @Override public String toString(boolean typed) {
        return "skip";
    }

    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
