/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.prettyprint;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;

/**
 * The Class TermTag is used to furnish {@link AnnotatedStringWithStyles} with
 * information on the presented terms.
 * 
 * <p>This information comprises information on the parent term, the index into
 * the list off all subterms of the toplevel term (totalpos) and the subterm
 * number to the direct parent.
 */
public class TermTag {

    /**
     * The number of the subterm to the direct parent.
     */
    private int subTermNo;
    
    /**
     * The term stored in this tag
     */
    private Term term;
    
    /**
     * The index into the array of all subterms of the toplevel term.
     */
    private int totalPos;
    
    /**
     * The tag from which this tag is derived, or null.
     */
    private TermTag parentTag;
    
    /**
     * Instantiates a new, underived term tag.
     * 
     * @param term
     *            the term to encapsulate
     */
    public TermTag(Term term) {
        this.term = term;
        this.subTermNo = -1;
        this.parentTag = null;
        this.totalPos = 0;
    }

    /**
     * Derive a new tag from this.
     * 
     * <p>
     * The argument is used as index into the subterms of the term of this tag.
     * The fields are set accordingly to keep track of the information.
     * 
     * @param index
     *            the index of the subterm
     * 
     * @return the term tag
     * 
     * @throws TermException
     *             if the index is not within the bounds of subterms
     */
    public TermTag derive(int index) throws TermException {
        Term subterm;
        try {
            subterm = term.getSubterm(index);
        } catch (IndexOutOfBoundsException e) {
            throw new TermException(e);
        }
        
        TermTag result = new TermTag(subterm);
        result.parentTag = this;
        result.subTermNo = index;
        
        result.totalPos = this.totalPos + 1;

        for(int i = 0; i < index; i++) {
            result.totalPos += term.getSubterm(i).countAllSubterms();
        }
        
        return result;
    }
    
    //
    // bunch of getters

    public int getSubTermNo() {
        return subTermNo;
    }

    public Term getTerm() {
        return term;
    }

    public int getTotalPos() {
        return totalPos;
    }

    public TermTag getParentTag() {
        return parentTag;
    }
    
    @Override public String toString() {
        return "TermTag[term=" + term + ";totalPos=" + totalPos + ";subtermNo="
                + subTermNo + "]";
    }
    
}
