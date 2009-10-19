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
     * <p>The argument needs to be a direct subterm od this's term.
     * The fields are set accordingly to keep track of the information.
     * 
     * @param t
     *            the t
     * 
     * @return the term tag
     * 
     * @throws TermException
     *             if t is not a direct subterm of the term stored in this.
     */
    public TermTag derive(Term t) throws TermException {
        TermTag result = new TermTag(t);
        result.parentTag = this;
        
        int total = totalPos + 1;

        for(int i = 0; i < term.countSubterms(); i++) {
            Term subterm = term.getSubterm(i);
            if(t.equals(subterm)) {
                result.subTermNo = i;
                result.totalPos = total;
                break;
            } else {
                total += subterm.countAllSubterms();
            }
        }
        
        if(result.subTermNo == -1)
            throw new TermException(t + " is no subterm of " + term);
        
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
