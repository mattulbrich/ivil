/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.rule;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.term.Term;

/**
 * AddModification models an add specification within a goal action in a rule.
 */
public class AddModification extends GoalModification {

    /**
     * The term to add to the sequent.
     */
    private Term termToAdd;

    /**
     * The location to add into the sequent. must be either
     * {@link MatchingLocation#ANTECEDENT} or {@link MatchingLocation#SUCCEDENT}.
     */
    private MatchingLocation locationToAdd;

    /**
     * Instantiates a new modification.
     * 
     * @param term
     *            the term to add
     * @param matchingLocation
     *            the location to add
     */
    public AddModification(@NonNull Term term,
            @NonNull MatchingLocation matchingLocation) {
        this.termToAdd = term;
        this.locationToAdd = matchingLocation;

        assert locationToAdd != MatchingLocation.BOTH;
    }

    @Override
    public @NonNull String toString() {
        switch (locationToAdd) {
        case ANTECEDENT:
            return "add " + termToAdd + " |-";
        case SUCCEDENT:
            return "add |- " + termToAdd;
        }
        // cannot be reached
        throw new Error();
    }
}
