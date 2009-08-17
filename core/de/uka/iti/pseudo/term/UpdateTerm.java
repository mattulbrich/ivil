/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.List;

import de.uka.iti.pseudo.term.statement.AssignmentStatement;

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
    
    private Update update;
    
    /**
     * Instantiates a new update term with the given update and the updated term.
     * 
     * @param update the update to apply
     * @param term the term to be updated
     */
    public UpdateTerm(Update update, Term term) {
        super(new Term[] { term }, term.getType());
        this.update = update;

        // TODO Auto-generated constructor stub
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
     * Gets the iummutable update object for this updated term
     * 
     * @return the update object
     */
    public Update getUpdate() {
        return update;
    }

}
