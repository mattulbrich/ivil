package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Environment;

public abstract class ProgramTerm extends Term {
    
    private boolean terminating;

    public ProgramTerm(boolean terminating) {
        super(Environment.getBoolType());
        this.terminating = terminating;
    }

    public boolean isTerminating() {
        return terminating;
    }
    
    public String toString(boolean typed) {
        if(isTerminating())
            return "[[ " + getContentString(typed) + "]]";
        else
            return "[ " + getContentString(typed) + "]";
    }

    protected abstract String getContentString(boolean typed);
}
