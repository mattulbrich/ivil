package de.uka.iti.pseudo.rule;

import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class LocatedTerm extends Pair<Term, MatchingLocation> {

    public LocatedTerm(Term term, MatchingLocation matchingLocation) {
        super(term, matchingLocation);
        // TODO Auto-generated constructor stub
    }

    public Term getTerm() {
        return fst();
    }
    
    public MatchingLocation getMatchingLocation() {
        return snd();
    }
    
    @Override public String toString() {
        switch(getMatchingLocation()) {
        case ANTECEDENT:
            return getTerm() + " |-";
        case SUCCEDENT:
            return "|- " + getTerm();
        case BOTH:
            return getTerm().toString();
        }
        // unreachable
        throw new Error();
    }

}
