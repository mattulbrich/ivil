/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Arrays;
import java.util.List;

import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class UpdateTerm encapsulates the application of an update (a list of
 * assignments) to a term.
 * 
 * The updates are stores as {@link AssignmentStatement} (like withing programs)
 * 
 * TODO We might reconsider the set of subterms to let it include ass. values.
 */
public class UpdateTerm extends Term {

    /**
     * The assignments are stored in an array which is not changed.
     */
    private AssignmentStatement[] assignments;

    /**
     * Instantiates a new update term with the given assignments in this 
     * order and the updated term.
     * 
     * @param assignments
     *            update terms stores a copy of this array
     * @param term
     *            the subterm of the result
     */
    public UpdateTerm(AssignmentStatement[] assignments, Term term) {
        super(new Term[] { term }, term.getType());
        this.assignments = assignments.clone();

        assert assignments.length > 0;
    }

    /**
     * Instantiates a new update term with the given assignments in this 
     * order and the updated term.
     * 
     * @param assignments
     *            update terms stores a copy of this list
     * @param term
     *            the subterm of the result
     */
    public UpdateTerm(List<AssignmentStatement> assignments, Term term) {
        super(new Term[] { term }, term.getType());
        this.assignments = Util.listToArray(assignments, AssignmentStatement.class);
        
        assert this.assignments.length > 0;
    }

    /*
     * equal to another update term if they have equal assignment sets
     * and equal subterms
     */
    public boolean equals(Object object) {
        if (object instanceof UpdateTerm) {
            UpdateTerm ut = (UpdateTerm) object;
            return Arrays.equals(assignments, ut.assignments)
                    && getSubterm(0).equals(ut.getSubterm(0));
        }
        return false;
    }

    /*
     * we do not print our own typing. the typing of 
     * the inner term suffices.
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

        if (typed)
            sb.append("(").append(getSubterm(0).toString(true)).append(")");
        else
            sb.append(getSubterm(0).toString(false));

        return sb.toString();
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Gets the assignments of the update of this update term.
     * 
     * @return an immutable list of assignments
     */
    public List<AssignmentStatement> getAssignments() {
        return Util.readOnlyArrayList(assignments);
    }

}
