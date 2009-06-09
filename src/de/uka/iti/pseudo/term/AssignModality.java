/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Function;

/**
 * The Class AssignModality captures assignments in modalities.
 */
public class AssignModality extends Modality {

    /**
     * The constant symbol to which something is assigned.
     */
    private Function assignedConstant;
    
    /**
     * The term which is assigned to the constant symbol
     */
    private Term assignedTerm;

    /**
     * Instantiates a new assign modality element.
     * 
     * @param assignedConstant
     *            the constant to assign to
     * @param assignedTerm
     *            the term to assign
     * 
     * @throws TermException
     *            if constant is not assignable, or type mismatch
     */
    public AssignModality(@NonNull Function assignedConstant, @NonNull Term assignedTerm)
            throws TermException {
        super();
        this.assignedConstant = assignedConstant;
        this.assignedTerm = assignedTerm;

        check();
    }

    /*
     * Check. correct term construction
     * 
     * fail if types mismatch or the constant is not assignable.
     */
    private void check() throws TermException {
        if (!assignedConstant.isAssignable())
            throw new TermException("The assigned symbol " + assignedConstant
                    + " is not assignable");
        
        if (!assignedConstant.getResultType().equals(assignedTerm.getType()))
            throw new TermException(
                    "The types of symbol and term differ.\nSymbol:"
                            + assignedConstant.getResultType() + "\nTerm:"
                            + assignedTerm.getType());
    }

    @Override
    public String toString(boolean typed) {
        return assignedConstant.getName() + ":=" + assignedTerm.toString(typed);
    }

    @Override
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public Function getAssignedConstant() {
        return assignedConstant;
    }

    public Term getAssignedTerm() {
        return assignedTerm;
    }

}
