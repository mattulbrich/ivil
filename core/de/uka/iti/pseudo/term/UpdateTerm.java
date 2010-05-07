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

import java.util.List;

import de.uka.iti.pseudo.term.statement.AssignmentStatement;

/**
 * The Class UpdateTerm encapsulates the application of an update (a list of
 * assignments) to a term.
 * 
 * Subterms are the updated terms and the update values (in this order). Please
 * note that the update targets are not subterms!
 * <p>The subterms of an update term are:
 * <ol>
 * <li>First the updated term
 * <li>Then all assigned values in order from left to right
 * </ol>
 * The updated <em>targets</em> are <b>not</b> subterms of an update term.
 */
public class UpdateTerm extends Term {
    
    private Update update;
    
    /**
     * Instantiates a new update term with the given update and the updated term.
     * 
     * @param update the update to apply
     * @param term the term to be updated
     */
    public UpdateTerm(Update update, Term term) {
        super(prepareSubterms(term, update), term.getType());
        this.update = update;
    }
    
    /*
     * prepare the subterms for the super class constructor.
     * First the updated term then all update values in order. 
     */
    private static Term[] prepareSubterms(Term term, Update update) {
        List<AssignmentStatement> assignments = update.getAssignments();
        Term[] result = new Term[assignments.size() + 1];
        
        result[0] = term;
        for (int i = 1; i < result.length; i++) {
            result[i] = assignments.get(i-1).getValue();
        }
        return result;
    }

    /*
     * equal to another update term if they have equal assignment sets
     * and equal subterms
     */
    public boolean equals(Object object) {
        if (object instanceof UpdateTerm) {
            UpdateTerm ut = (UpdateTerm) object;
            return update.equals(ut.update)
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

        sb.append(update.toString(typed));

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
     * the call is delegated to the update
     * 
     * @return an immutable list of assignments
     */
    public List<AssignmentStatement> getAssignments() {
        return update.getAssignments();
    }

    /**
     * Gets the immutable update object for this updated term
     * 
     * @return the update object
     */
    public Update getUpdate() {
        return update;
    }

}
