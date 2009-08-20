package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public class GotoStatement extends Statement {

    public GotoStatement(int sourceLineNumber, Term[] targets) throws TermException {
        super(sourceLineNumber, targets);
    }

    public String toString(boolean typed) {
        return super.toString(typed) + 
            "goto " + Util.commatize(getSubterms());
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
