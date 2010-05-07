/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Arrays;
import java.util.List;

import nonnull.NonNull;

import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class Update captures a set of assignments to assignable variables.
 */
public class Update {

    /**
     * Instantiates a new update using an array of assignments.
     * The array is cloned before using it internally.
     * 
     * @param assignments
     *            a non-empty array of assignments
     */
    public Update(@NonNull AssignmentStatement[] assignments) {
        assert assignments.length > 0;
        this.assignments = assignments.clone();
    }

    /**
     * Instantiates a new update using a list of assignments.
     * The list is copied before it is used internally.
     * 
     * @param assignments
     *            a non-empty list of assignments
     */
    public Update(List<AssignmentStatement> assignments) {
        assert assignments.size() > 0;
        this.assignments = Util.listToArray(assignments, AssignmentStatement.class);
    }

    /**
     * The assignments are stored in an array which is not changed.
     */
    private AssignmentStatement[] assignments;
    
    
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

        for (int i = 0; i < assignments.length; i++) {
            if (i == 0)
                sb.append("{ ");
            else
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
    public List<AssignmentStatement> getAssignments() {
        return Util.readOnlyArrayList(assignments);
    }
    
    /**
     * This is equal to another Update if the assignments coincide verbatim
     * (including their order!)
     */
    public boolean equals(Object obj) {
        if (obj instanceof Update) {
            Update up = (Update) obj;
            return Arrays.equals(assignments, up.assignments);
        }
        return false;
    }
    
    /**
     * The hash code of an update is the hash code the assignments array seen as
     * a list.
     */
    public int hashCode() {
        return Util.readOnlyArrayList(assignments).hashCode();
    }
    
}
