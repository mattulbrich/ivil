/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;

/**
 * The Class TermSelector is used to select a subterm from a sequent.
 * It description consists of 3 parts:
 * <ol>
 * <li>The side of the sequent, either ANTECEDENT or SUCCEDENT
 * <li>The number of the term on that side (starting at 0)
 * <li>The number of the subterm in that term (0 being the term itself)
 * </ol>
 * The string representation of a TermSelector is of the form
 * <pre>(A|S).number.number</pre>, for instance <code>A.1.0</code> for the
 * whole second term on the antecedent side.
 * 
 * <p>The term which the selected term is a subterm of is referred to 
 * as the top term and the actually selected term is referred to as
 * the subterm.
 */
public class TermSelector {

    /**
     * The Constant ANTECEDENT is equivalent to true
     */
    public static final boolean ANTECEDENT = true;
    
    /**
     * The Constant SUCCEDENT is equivalent to false
     */
    public static final boolean SUCCEDENT = false;
    
    /**
     * We store the side as a boolean value. True iff in antecedent.
     */
    private boolean inAntecedent;
    
    /**
     * The number of the top term.
     */
    private int termNo;
    
    /**
     * The number of the subterm in the top term
     */
    private int subtermNo;
    
    /**
     * Instantiates a new term selector from the three partial informations
     * 
     * @param inAntecedent the side of the sequent
     * @param termNo the term number
     * @param subtermNo the subterm number
     */
    public TermSelector(boolean inAntecedent, int termNo, int subtermNo) {
        this.inAntecedent = inAntecedent;
        this.termNo = termNo;
        this.subtermNo = subtermNo;
        
        assert termNo >= 0;
        assert subtermNo >= 0;
    }

    /**
     * Instantiates a new term selector from a term selector and a subterm
     * number.
     * 
     * <p>
     * The created selector has a sequent side and toplevel number of the
     * argument, only the subterm number is replaced.
     * 
     * @param termSelector
     *            the term selector to modify
     * @param subtermNo
     *            the subterm number to replace with
     */
    public TermSelector(TermSelector termSelector, int subtermNo) {
        this.inAntecedent = termSelector.inAntecedent;
        this.termNo = termSelector.termNo;
        this.subtermNo = subtermNo;
    }

    
    /**
     * Instantiates a new term selector which refers to a toplevel term.
     * 
     * The resulting {@link TermSelector} has a subterm number of 0.
     * 
     * @param inAntecendent the side of the sequent
     * @param termNo the term number
     */
    public TermSelector(boolean inAntecendent, int termNo) {
        this(inAntecendent, termNo, 0);
    }
    
    /**
     * Instantiates a new term selector from a string description.
     * 
     * The first character needs to be either A or S followed by a dot
     * followed by a non-negative number followed by a dot and another
     * non-negative number
     * 
     * @param descr a string description of a TermSelector
     * 
     * @throws FormatException if the string is incorrectly formatted
     */
    public TermSelector(String descr) throws FormatException {
        String[] sect = descr.split("\\.");
        if(sect.length != 3)
            throw new FormatException("TermSelector", "illegally separated string", descr);
        
        if("A".equals(sect[0])) {
            inAntecedent = true;
        } else if("S".equals(sect[0])) {
            inAntecedent = false;
        } else
            throw new FormatException("TermSelector", "unknown first part: " + sect[0], descr);
        
        try {
            termNo = Integer.parseInt(sect[1]);
            if(termNo < 0)
                throw new FormatException("TermSelector", "negative: " + sect[1], descr);
        } catch (NumberFormatException e) {
            throw new FormatException("TermSelector", "not a number: " + sect[1], descr);
        }
        
        try {
            subtermNo = Integer.parseInt(sect[2]);
            if(subtermNo < 0)
                throw new FormatException("TermSelector", "negative: " + sect[2], descr);
        } catch (NumberFormatException e) {
            throw new FormatException("TermSelector", "not a number: " + sect[2], descr);
        }
    }

    /**
     * a string representation of the TermSelector.
     * We create an equal object if parsing this string 
     * using {@link TermSelector#TermSelector(String).
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return (inAntecedent ? "A." : "S.") + termNo + "." + subtermNo;
    }
    
    /**
     * An object is equal to this if it is a TermSelector and all
     * three indicators have the same value.
     * @param obj object to compare to
     * @return true iff obj refers to the same term as this
     */
    public boolean equals(Object obj) {
        if (obj instanceof TermSelector) {
            TermSelector ts = (TermSelector) obj;
            return ts.isAntecedent() == ts.isAntecedent() 
                    && ts.getTermNo() == getTermNo()
                    && ts.getSubtermNo() == getSubtermNo();
        }
        return false;
    }

    /**
     * check whether the selection refers to the antecedent side of 
     * a sequent 
     * @return true, if the selection is on the antecedent soide 
     */
    public boolean isAntecedent() {
        return inAntecedent;
    }
    
    /**
     * check whether the selection refers to the succedent side of 
     * a sequent 
     * @return true, if the selection is on the succedent soide
     */
    public boolean isSuccedent() {
        return !inAntecedent;
    }

    /**
     * Gets the number of the toplevel term to which the
     * selection refers to
     * 
     * @return the number of the term (starting at 0)
     */
    public int getTermNo() {
        return termNo;
    }

    /**
     * Checks if this selection refers to a top level term
     * 
     * @return true, if the subterm number is equal to 0
     */
    public boolean isToplevel() {
        return subtermNo == 0;
    }
    
    /**
     * Gets the subterm number
     * 
     * @return the subterm number
     */
    public int getSubtermNo() {
        return subtermNo;
    }

    /**
     * If this selector selects a top level term, then this method
     * selects a subterm of the term.
     * 
     * @param subtermNo the subterm number of the term to select
     * 
     * @return a TermSelector with side and term number as this 
     * object and the subterm subtermNo
     */
    public TermSelector selectSubterm(int subtermNo) {
        assert subtermNo >= 0 : subtermNo;
        return new TermSelector(inAntecedent, termNo, subtermNo);
    }
    
    /**
     * Apply the term selector to a sequent to select a particular 
     * top level term. This method ignores the subterm number.
     * 
     * <p>The selection can fail, if an index (either term index or
     * subterm index) are out of range.
     * 
     * @param sequent the sequent to select from
     * 
     * @return the term to which the selector refers
     * 
     * @throws ProofException if the selection cannot be 
     * applied to the sequent.
     */
    public Term selectTopterm(@NonNull Sequent sequent) throws ProofException {
        List<Term> terms;
        if (isAntecedent()) {
            terms = sequent.getAntecedent();
        } else {
            terms = sequent.getSuccedent();
        }

        int termNo = getTermNo();
        if (termNo < 0 || termNo >= terms.size())
            throw new ProofException("Can select " + this);

        return terms.get(termNo);
    }
    
    /**
     * Apply the term selector to a sequent to select a particular 
     * term. This method takes the subterm number into consideration.
     * 
     * <p>The selection can fail, if an index (either term index or
     * subterm index) are out of range.
     * 
     * @param sequent the sequent to select from
     * 
     * @return the term to which the selector refers
     * 
     * @throws ProofException if the selection cannot be 
     * applied to the sequent.
     */
    public Term selectSubterm(@NonNull Sequent sequent) throws ProofException {
        Term term = selectTopterm(sequent);
        if (isToplevel()) {
            return term;
        } else {
            List<Term> subterms = SubtermCollector.collect(term);

            int subtermNo = getSubtermNo();
            if (subtermNo < 0 || subtermNo >= subterms.size())
                throw new ProofException("Can select " + this);

            return subterms.get(subtermNo);
        }
    }
    
}
