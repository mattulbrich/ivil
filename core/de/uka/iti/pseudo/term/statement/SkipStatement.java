package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public class SkipStatement extends Statement {

    public SkipStatement(int sourceLineNumber, Term[] arguments) throws TermException {
        super(sourceLineNumber, arguments);
    }
    
    // convenience constructor
    public SkipStatement() throws TermException {
        this(-1, new Term[0]);
    }

    public String toString(boolean typed) {
        if(countSubterms() == 0)
            return "skip";
        else
            return "skip_loopinv " + Util.commatize(getSubterms());
    }

    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
