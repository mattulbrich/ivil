package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public class GotoStatement extends Statement {

    public GotoStatement(Term[] targets) throws TermException {
        super(targets);
    }

    public String toString(boolean typed) {
        return "goto " + Util.commatize(getSubterms());
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public boolean equals(Object object) {
        if (object instanceof GotoStatement) {
            GotoStatement gotoSt = (GotoStatement) object;
            return getSubterms().equals(gotoSt.getSubterms());
        }
        return false;
    }

}
