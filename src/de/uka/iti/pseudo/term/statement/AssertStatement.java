package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;


public class AssertStatement extends Statement {

    public AssertStatement(Term conditionTerm) throws TermException {
        super(conditionTerm);
    }

    public String toString(boolean typed) {
        return "assert " + getConditionTerm().toString(typed);
    }

}
