package de.uka.iti.pseudo.rule;

import de.uka.iti.pseudo.term.Term;

public class ReplaceModification extends GoalModification {
    
    private Term termToReplace;

    public ReplaceModification(Term term) {
        this.termToReplace = term;
    }
    
    @Override public String toString() {
        return "replace " + termToReplace;
    }

}
