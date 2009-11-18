package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public class GotoStatement extends Statement {

    public GotoStatement(int sourceLineNumber, Term[] targets) throws TermException {
        super(sourceLineNumber, targets);
        
        for (int i = 0; i < targets.length; i++) {
            assert targets[i] != null;
        }
    }

    public String toString(boolean typed) {
        return  
            "goto " + Util.commatize(getSubterms());
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
