/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.term.creation;

import java.util.ArrayList;
import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

/**
 * The Class SubtermCollector provides a static method {@link #collect(Term)}
 * which allows to get a list of all subterms of a term. The result is
 * deterministic, it is the same for a term every time the method is called.
 * 
 * <p>IMPORTANT! Keep the order in this visitor synchronized with the related 
 * visitors {@link PrettyPrint}
 */
public class SubtermCollector extends DefaultTermVisitor.DepthTermVisitor {
    
    /*
     * The list of so far collected subterms
     */
    private List<Term> subtermsInOrder = new ArrayList<Term>();
    
    /*
     * constructor hidden to avoid unwanted objects
     */
    protected SubtermCollector() { }
    
    /**
     * Collect all subterms of a term.
     * 
     * This includes all subterms in updates and the term itself.
     * 
     * @param term
     *            the term for which a list of subterms is to be retrieved.
     * 
     * @return the list of subterms in term
     */
    public static @NonNull List<Term> collect(@NonNull Term term) {
        SubtermCollector sc = new SubtermCollector();
        try {
            term.visit(sc);
        } catch (TermException e) {
            // never thrown in this code
            throw new Error(e);
        }
        return sc.subtermsInOrder;
    }
    
    protected void defaultVisitTerm(Term term) throws TermException {
        subtermsInOrder.add(term);
        for (Term t : term.getSubterms()) {
            t.visit(this);
        }
    }
    
    /*
     * in updates the values are considered subterms on which rules can be applied.
     */
    public void visit(UpdateTerm updateTerm) throws TermException {
        // first the term itself
        subtermsInOrder.add(updateTerm);

        // then the update values
        for (AssignmentStatement ass : updateTerm.getAssignments()) {
            ass.getValue().visit(this);
        }
        
        // then the updated term
        updateTerm.getSubterm(0).visit(this);
    }

}
