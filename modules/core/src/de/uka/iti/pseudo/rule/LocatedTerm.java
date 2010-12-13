/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule;

import nonnull.NonNull;
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
public class LocatedTerm extends Pair</*@NonNull*/ Term, /*@NonNull*/MatchingLocation> {

    /**
     * Instantiates a new located term.
     * 
     * @param term the term
     * @param matchingLocation the matching location
     */
    public LocatedTerm(@NonNull Term term, @NonNull MatchingLocation matchingLocation) {
        super(term, matchingLocation);
    }

    public Term getTerm() {
        return fst();
    }
    
    public MatchingLocation getMatchingLocation() {
        return snd();
    }

    /**
     * convert the term to a string and add the sequent separator "|-" either
     * before or behind the term if appropriate
     * 
     * return a term with typing information iff {@link Term#SHOW_TYPES} is set
     * to true.
     * 
     * @return located term as string
     */
    @Override public String toString() {
        return toString(Term.SHOW_TYPES);
    }
    
    /**
     * convert the term to a string and add the sequent separator "|-" either
     * before or behind the term if appropriate
     * 
     * return a term with typing information iff {@code showTypes} is set
     * to true.
     * 
     * @param showTypes
     *            iff true print the located term with typing information
     * 
     * @return located term as string
     */
    public String toString(boolean showTypes) {
        switch(getMatchingLocation()) {
        case ANTECEDENT:
            return getTerm() + " |-";
        case SUCCEDENT:
            return "|- " + getTerm();
        case BOTH:
            return getTerm().toString(showTypes);
        }
        // unreachable
        throw new Error();
    }

    /**
     * Checks whether this located term could possibly fit a term selector.
     * This is the case if the selector is on "the same side" of
     * the sequent or if the term is not bound to a side.
     * Bugfix: Also, if constraint, it needs to be a toplevel term. 
     * 
     * @param selector the selector
     * 
     * @return true, if the selector can be used to match this.
     */
    public boolean isFittingSelect(TermSelector selector) {
        switch(getMatchingLocation()) {
        case ANTECEDENT:
            return selector.isAntecedent() && selector.isToplevel();
        case SUCCEDENT:
            return selector.isSuccedent() && selector.isToplevel();
        case BOTH:
            return true;
        }
        // unreachable
        throw new Error();
    }

}
