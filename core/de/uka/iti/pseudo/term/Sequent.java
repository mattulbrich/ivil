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

import nonnull.NonNull;

import de.uka.iti.pseudo.term.creation.ToplevelCheckVisitor;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Sequent is used to model immutable logic sequents. A sequent consists of lists of terms. Any term 
 * in these lists needs to be of boolean type. 
 */
public class Sequent {
    
    /**
     * The antecedent.
     */
    private Term[] antecedent;
    
    /**
     * The succedent.
     */
    private Term[] succedent;
    
    /**
     * Instantiates a new sequent.
     * 
     * The given arrays are not stored in the sequent themselves but are (shallow-) copied first. You can savely change them 
     * after the constructor call. 
     * 
     * @param antecedent
     *            the terms in the antecedent
     * @param succedent
     *            the terms in the succedent
     * 
     * @throws TermException
     *             if a term is not suitable for toplevel usage.
     */
    public Sequent(@NonNull Term[] antecedent, @NonNull Term[] succedent) throws TermException {
        this(Arrays.asList(antecedent), Arrays.asList(succedent));
    }

    /**
     * Instantiates a new sequent.
     * 
     * The given lists are not stored in the sequent themselves but are (shallow-) copied first. You can savely change them 
     * after the constructor call. 
     * 
     * @param antecedent
     *            the terms in the antecedent
     * @param succedent
     *            the terms in the succedent
     * 
     * @throws TermException
     *             if a term is not suitable for toplevel usage.
     */
    public Sequent(List<Term> antecedent, List<Term> succedent) throws TermException {
        this.antecedent = Util.listToArray(antecedent, Term.class);
        this.succedent = Util.listToArray(succedent, Term.class);
        check();
    }

    /*
     * Check whether all terms are usable as formulas: No schema variables, boolean type, etc.
     * See TopLevelVisitor for details.
     */
    private void check() throws TermException {
        ToplevelCheckVisitor checker = new ToplevelCheckVisitor();
        for (Term t : antecedent) {
            t.visit(checker);
        }
        for (Term t : succedent) {
            t.visit(checker);
        }
    }

    /**
     * Gets an immutable view on the terms in the antecedent.
     * 
     * @return the terms in the antecedent
     */
    public @NonNull List<Term> getAntecedent() {
        return Util.readOnlyArrayList(antecedent);
    }
    
    /**
     * Gets an immutable view on the terms in the succedent.
     * 
     * @return the terms in the succedent
     */
    public @NonNull List<Term> getSuccedent() {
        return Util.readOnlyArrayList(succedent);
    }
    
    /**
     * A sequent is represented as a string as two comma separated lists of term strings
     * separated by <code>|-</code>. The antecedent appears on the left of the separator and the succedent
     * on the right, like in
     * <pre>
     *    ante1, ante2, ..., anteN |- succ1, succ2, ..., succM
     * </pre>
     * Both, antecedent and succedent may be the empty list and the string therefore empty.  
     */    
    @Override 
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Term t : antecedent) {
            sb.append(t).append(" ");
        }
        sb.append("|-");
        for (Term t : succedent) {
            sb.append(" ").append(t);
        }
        return sb.toString();
    }
    
}
