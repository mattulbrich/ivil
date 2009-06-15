package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public abstract class Statement {

    private Term conditionTerm;

    public Statement(Term conditionTerm) throws TermException {
        this.conditionTerm = conditionTerm;
        if(conditionTerm.getType().equals(Environment.getBoolType()))
            throw new TermException("This statement expects a boolean condition, but received " + conditionTerm);
    }
    
    public Statement() {
        conditionTerm = null;
    }
    
    public Term getConditionTerm() {
        return conditionTerm;
    }

    public abstract String toString(boolean typed);
}
