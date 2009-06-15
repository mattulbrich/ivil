package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class EndStatement extends Statement {

    public EndStatement(Term conditionTerm) throws TermException {
        super(conditionTerm);
    }
    
    public String toString(boolean typed) {
        return "end " + getConditionTerm().toString(typed);
    }

}
