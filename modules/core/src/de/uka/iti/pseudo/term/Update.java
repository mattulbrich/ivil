/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Arrays;
import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;

import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Update captures a set of assignments to assignable variables.
 */
public class Update {

    /**
     * An empty update. It has no assignments.
     */
    public static final Update EMPTY_UPDATE = new Update();
    
    /**
     * The assignments are stored in an array which is not changed.
     */
    private Assignment[] assignments;


    /**
     * Instantiates a new update using an array of assignments.
     * The array is cloned before using it internally.
     * 
     * @param assignments
     *            a non-empty array of assignments
     */
    public Update(@NonNull Assignment[] assignments) {
        assert assignments.length > 0;
        this.assignments = assignments.clone();
    }
    
    /**
     * Instantiates a new empty update.
     */
    private Update() {
        this.assignments = new Assignment[0];
    }

    /**
     * Instantiates a new update using a list of assignments.
     * The list is copied before it is used internally.
     * 
     * @param assignments
     *            a non-empty list of assignments
     */
    public Update(List<Assignment> assignments) {
        assert assignments.size() > 0;
        this.assignments = Util.listToArray(assignments, Assignment.class);
    }

    /**
     * Put the update into a string. We do not print our typing, the typing of
     * the assigned terms suffices.
     * 
     * @param typed
     *            whether or not the assigned values are to be printed typed.
     * 
     * @return string representation of this update in enclosed in "{", "||" and
     *         "}"
     */
    public String toString(boolean typed) {
        StringBuilder sb = new StringBuilder();

        sb.append("{ ");
        for (int i = 0; i < assignments.length; i++) {
            if (i != 0)
                sb.append(" || ");
            sb.append(assignments[i].toString(typed));
        }
        sb.append(" }");

        return sb.toString();
    }
    
    @Override public String toString() {
        return toString(Term.SHOW_TYPES);
    }
    
    /**
     * Gets the assignments of the update of this update term.
     * 
     * @return an immutable list of assignments
     */
    public List<Assignment> getAssignments() {
        return Util.readOnlyArrayList(assignments);
    }
    
    /**
     * This is equal to another Update if the assignments coincide verbatim
     * (including their order!)
     */
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Update) {
            Update up = (Update) obj;
            return Arrays.equals(assignments, up.assignments);
        }
        return false;
    }
    
    /**
     * Checks if this update is empty, hence contains no assignments.
     * 
     * @return true, iff this is empty
     */
    public boolean isEmpty() {
        return assignments.length == 0;
    }

    /**
     * The hash code of an update is the hash code of the assignments array.
     */
    public int hashCode() {
        return Arrays.hashCode(assignments);
    }
    
}
