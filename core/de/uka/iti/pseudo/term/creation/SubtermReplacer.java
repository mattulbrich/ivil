/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import nonnull.NonNull;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * The SubtermReplacer visitor is used to replace one particular subterm in a
 * term.
 * 
 * The term to be substituted is either referred to by a {@link TermSelector} or
 * by an index into the list of enumerated subterms of the term. Internally the
 * latter is used to find the term to replace.
 */
public class SubtermReplacer extends RebuildingTermVisitor {
    
    /**
     * The counter of processed subterms
     */
    private int counter;
    
    /**
     * The number of the subterm to replace
     */
    private int replaceNumber;
    
    /**
     * replace subterm with this term
     */
    private Term replaceWith;
    
    /**
     * Instantiates a new subterm replacer.
     * 
     * @param replaceNumber
     *            the number of the subterm to replace
     * @param replaceWith
     *            the term to replace the subterm with
     */
    private SubtermReplacer(int replaceNumber, @NonNull Term replaceWith) {
        this.replaceNumber = replaceNumber;
        this.replaceWith = replaceWith;
    }
    
    /**
     * Replace a subterm within a larger term
     * 
     * @param term
     *            the term in which the replacement is to take place
     * @param subtermNo
     *            the number of the subterm to be replaced
     * @param replaceWith
     *            the term to replace the subterm with
     * 
     * @return the original term with the indicated subterm substututed by
     *         replaceWith, null if the index is outside the number of subterms
     *         in this term
     * 
     * @throws TermException
     *             for instance if the new term cannot be typed or construction
     *             fails otherwise.
     */
    public static Term replace(Term term, int subtermNo, Term replaceWith) throws TermException {
        SubtermReplacer str = new SubtermReplacer(subtermNo, replaceWith);
        term.visit(str);
        return str.resultingTerm;
    }
    
    
    /**
     * Replace a subterm within a term
     * 
     * @param term
     *            the term in which the replacement is to take place
     * @param sel
     *            the selector used to identify the subterm to be replaced.
     * @param replaceWith
     *            the term to replace the subterm with
     * 
     * @return the original term with the indicated subterm substututed by
     *         replaceWith, null if the index is outside the number of subterms
     *         in this term
     * 
     * @throws TermException
     *             for instance if the new term cannot be typed or construction
     *             fails otherwise.
     */
    public static Term replace(Term term, SubtermSelector sel, Term replaceWith) throws TermException {
        return replace(term, sel.getLinearIndex(term), replaceWith);
    }
    
    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.creation.RebuildingTermVisitor#defaultVisitTerm(de.uka.iti.pseudo.term.Term)
     */
    @Override 
    protected void defaultVisitTerm(Term term) throws TermException {
        if(counter == replaceNumber)
            resultingTerm = replaceWith;
        else
            resultingTerm = null;
        counter ++;
    }

    
}
