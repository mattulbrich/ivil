/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.rule;

import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

/**
 * A LocatedTerm is a combination of a term and its location.
 * 
 * <p>The location may be one of the constants of the enum {@link MatchingLocation}.
 * 
 */
public class LocatedTerm extends Pair<Term, MatchingLocation> {

    /**
     * Instantiates a new located term.
     * 
     * @param term the term
     * @param matchingLocation the matching location
     */
    public LocatedTerm(Term term, MatchingLocation matchingLocation) {
        super(term, matchingLocation);
    }

    public Term getTerm() {
        return fst();
    }
    
    public MatchingLocation getMatchingLocation() {
        return snd();
    }
    
    /**
     * convert the term to a string and add the sequent separator
     * "|-" either before or behind the term if appropriate
     * 
     * @return located term as string
     */
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

    /**
     * Checks whether this located term could possibly fit a term selector.
     * This is the case if the selector is on "the same side" of
     * the sequent or if the term is not bound to a side.
     * 
     * @param selector the selector
     * 
     * @return true, if the selector can be used to match this.
     */
    public boolean isFittingSelect(TermSelector selector) {
        switch(getMatchingLocation()) {
        case ANTECEDENT:
            return selector.isAntecedent();
        case SUCCEDENT:
            return selector.isSuccedent();
        case BOTH:
            return true;
        }
        // unreachable
        throw new Error();
    }

}
