package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Environment;

public abstract class ProgramTerm extends Term {
    
    private boolean terminating;

    public ProgramTerm(boolean terminating) {
        super(Environment.getBoolType());
        this.terminating = terminating;
    }
    
    public ProgramTerm(Term[] substring, boolean terminating) {
        super(substring, Environment.getBoolType());
        this.terminating = terminating;
    }

    public boolean isTerminating() {
        return terminating;
    }
    
    public String toString(boolean typed) {
        String res;
        if(isTerminating())
            res =  "[[" + getContentString(typed) + "]]";
        else
            res =  "[" + getContentString(typed) + "]";
        
        if(typed)
            res += " as bool";
        
        return res;
    }

    protected abstract String getContentString(boolean typed);
}
