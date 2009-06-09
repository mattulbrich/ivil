package de.uka.iti.pseudo.rule;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.term.Term;

public class AddModification extends GoalModification {
    
    private Term termToAdd;
    private MatchingLocation locationToAdd;

    public AddModification(@NonNull Term term,
            @NonNull MatchingLocation matchingLocation) {
        this.termToAdd = term;
        this.locationToAdd = matchingLocation;
        
        assert locationToAdd != MatchingLocation.BOTH;
    }

    @Override
    public String toString() {
        switch(locationToAdd) {
        case ANTECEDENT: return "add " + termToAdd + " |-";
        case SUCCEDENT: return "add |- " + termToAdd;
        }
        // cannot be reached
        throw new Error();
    }
}
