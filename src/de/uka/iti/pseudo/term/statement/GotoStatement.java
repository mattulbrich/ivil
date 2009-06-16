package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public class GotoStatement extends Statement {

    public GotoStatement(Term[] targets) throws TermException {
        super(targets);
    }

    @Override public String toString(boolean typed) {
        return "goto " + Util.commatize(getSubterms());
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }


}