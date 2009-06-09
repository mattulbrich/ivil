/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.rule;

import java.util.List;

import nonnull.NonNull;

/**
 * The Class GoalAction encapsulates a list of action to take on a goal. Such an
 * action can start a new goal, copy the current goal or close the current goal.
 */
public class GoalAction {

    /**
     * The enumeration of kinds of actions.
     */
    enum Kind {
        CLOSE, COPY, NEW
    }

    private @NonNull Kind kind;
    
    
    private @NonNull GoalModification[] modifications;

    /**
     * Instantiates a new goal action.
     * 
     * @param kindString
     *            a string describing the kind of action
     * @param mods
     *            a list of modifications for this action. must be empty if
     *            kindString is closegoal.
     * 
     * @throws RuleException
     *             might be thrown in the future
     * @throws IllegalArgumentException
     *             if kindString is not valid
     */
    public GoalAction(@NonNull String kindString, 
            @NonNull List<GoalModification> mods)
            throws RuleException {

        if (kindString.equals("closegoal")) {
            this.kind = Kind.CLOSE;
        } else if (kindString.equals("newgoal")) {
            this.kind = Kind.NEW;
        } else if (kindString.equals("copygoal")) {
            this.kind = Kind.COPY;
        } else
            throw new IllegalArgumentException();

        // CLOSE implies empty
        assert this.kind != Kind.CLOSE || mods.isEmpty();

        this.modifications = mods.toArray(new GoalModification[mods.size()]);
    }

    public void dump() {
        System.out.println("      action " + kind);

        for (GoalModification mod : modifications) {
            System.out.println("        " + mod);
        }
    }

}
