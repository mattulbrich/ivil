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
import de.uka.iti.pseudo.term.AssignModality;
import de.uka.iti.pseudo.term.IfModality;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.WhileModality;

/**
 * The Class SubtermCollector provides a static method {@link #collect(Term)}
 * which allows to get a list of all subterms of a term. The result is
 * deterministic, it is the same for a term every time the method is called.
 */
public class SubtermCollector extends DefaultTermVisitor {
    
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
     * This includes all terms in modalities and the term itself.
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
    
    protected void defaultVisitModality(Modality modality)
            throws TermException {
        for (Modality m : modality.getSubModalities()) {
            m.visit(this);
        }
    }

    protected void defaultVisitTerm(Term term) throws TermException {
        subtermsInOrder.add(term);
        for (Term t : term.getSubterms()) {
            t.visit(this);
        }
    }

    public void visit(ModalityTerm modalityTerm) throws TermException {
        subtermsInOrder.add(modalityTerm);
        modalityTerm.getModality().visit(this);
        for (Term t : modalityTerm.getSubterms()) {
            t.visit(this);
        }
    }

    public void visit(AssignModality assignModality)
            throws TermException {
        defaultVisitTerm(assignModality.getAssignedTerm());
    }

    public void visit(IfModality ifModality) throws TermException {
        defaultVisitTerm(ifModality.getConditionTerm());
        defaultVisitModality(ifModality);
    }

    public void visit(WhileModality whileModality)
            throws TermException {
        defaultVisitTerm(whileModality.getConditionTerm());
        defaultVisitModality(whileModality);
    }

}
