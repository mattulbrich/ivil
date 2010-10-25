/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

/**
 * The Class TermSelector is used to select a subterm from a sequent. It
 * description consists of 2 parts:
 * <ol>
 * <li>The side of the sequent, either ANTECEDENT or SUCCEDENT
 * <li>The number of the term on that side (starting at 0)
 * <li>The path to the subterm in that term (list of variable length)
 * </ol>
 * 
 * <p>
 * The string representation of a TermSelector is of the form
 * <pre>
 * (A|S).number.number*
 * </pre>, for instance <code>A.1</code> for the whole second term on the
 * antecedent side and <code>S.2.0.1</code> for the the second subterm of the
 * first subterm of the third formula in the succedent.
 * 
 * <p>
 * The term which the selected term is a subterm of is referred to as the top
 * term and the actually selected term is referred to as the subterm.
 * 
 * <p>
 * We silently assume that any side of any sequent has less than 128 formulas
 * and no term has more than 127 subterms. This is also checked by assertions.
 * 
 * @see SubtermSelector
 */
public class TermSelector {
    
    private static final SubtermSelector EMPTY_SUBTERMSELECTOR = new SubtermSelector();

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
     * the number of the selected term on its side.
     */
    private byte termNumber;
    
    /**
     * If a subterm of the term is selected, then this field contains
     * the path to it.
     * 
     * Note that an empty subterm selector can also refer to the top
     * level term as the 0th subterm.
     */
    private @NonNull SubtermSelector subtermSelector;
    
    /**
     * Instantiates a new term selector from path informations
     * 
     * @param inAntecedent
     *            the side of the sequent
     * @param termNo
     *            the toplevel term number
     * @param path
     *            the path to the subterm in the given term
     */
    public TermSelector(boolean inAntecedent, int termNo, int... path) {
        assert termNo >= 0 && termNo <= Byte.MAX_VALUE;
        
        this.inAntecedent = inAntecedent;
        this.termNumber = (byte) termNo;
        // FIXME can this ever be null
        if(path != null && path.length > 0)
            this.subtermSelector = new SubtermSelector(path);
        else
            this.subtermSelector = EMPTY_SUBTERMSELECTOR;
    }

    /**
     * Instantiates a new term selector from a term selector and a subterm
     * number.
     * 
     * <p>
     * The created selector contains the same information as the argument.
     * Only the path to the subterm is augmented by the given path.
     * 
     * @param termSelector
     *            the term selector to modify
     * @param path
     *            the path to select
     */
    public TermSelector(TermSelector termSelector, int... path) {
        this.inAntecedent = termSelector.inAntecedent;
        this.termNumber = termSelector.termNumber;
        this.subtermSelector = new SubtermSelector(termSelector.subtermSelector, path);
    }

    /**
     * Instantiates a new term selector from a string description.
     * 
     * The first character needs to be either 'A' or 'S' followed by a dot followed
     * by a non-negative number followed by a dot and another non-negative
     * number, etc. As production this is:
     * <pre>
     * TermSelector ::= ( 'A' | 'S' ) ( '.' NonNegNumber )+
     * </pre>
     * <p>The result of {@link #toString()} can be parsed in again.
     * 
     * @param descr
     *            a string description of a TermSelector
     * 
     * @throws FormatException
     *             if the string is incorrectly formatted
     */
    public TermSelector(String descr) throws FormatException {
        
        if (descr.startsWith(".") || descr.endsWith("."))
            throw new FormatException("TermSelector", "Illegal character at start/end: .", descr);
        
        String[] sect = descr.split("\\.", 3);
        
        if (sect.length < 2) {
            throw new FormatException("TermSelector", "Term selector needs at least 2 parts:", descr);
        }

        if ("A".equals(sect[0])) {
            inAntecedent = true;
        } else if ("S".equals(sect[0])) {
            inAntecedent = false;
        } else
            throw new FormatException("TermSelector", "unknown first part: " + sect[0], descr);

        if (sect[1].length() == 0) {
            throw new FormatException("TermSelector", "empty term number", descr);
        }
         
        try {
            termNumber = Byte.parseByte(sect[1]);
            if (termNumber < 0) {
                throw new FormatException("TermSelector", "negative: "
                        + sect[1], descr);
            }
        } catch (NumberFormatException e) {
            throw new FormatException("TermSelector", "not a number: "
                    + sect[1], descr);
        }
        
        if (sect.length == 3)
            subtermSelector = new SubtermSelector(sect[2]);
        else
            subtermSelector = EMPTY_SUBTERMSELECTOR;

    }

    /**
     * a string representation of the TermSelector. We create an equal object if
     * parsing this string using {@link TermSelector#TermSelector(String)}.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(inAntecedent ? "A." : "S.");
        sb.append(termNumber);
        String st = subtermSelector.toString();
        if(st.length() > 0) {
            sb.append(".").append(st);
        }
        return sb.toString();
    }
    
    /**
     * Gets the depth of this selector.
     * The depth is the number of sub term selections needed to find the desired term.
     * It is the length of the path plus one (for selecting the top level term)
     * 
     * @return a number >= 0
     */
    public int getDepth() {
        return subtermSelector.getDepth();
    }

    /**
     * An object is equal to this if it is a TermSelector and all three
     * indicators have the same value.
     * 
     * @param obj
     *            object to compare to
     * @return true iff obj refers to the same term as this
     */
    public boolean equals(Object obj) {
        if (obj instanceof TermSelector) {
            TermSelector ts = (TermSelector) obj;
            if(isAntecedent() != ts.isAntecedent())
                return false;
            
            if(getDepth() != ts.getDepth())
                return false;
            
            return subtermSelector.equals(ts.subtermSelector);
        }
        return false;
    }
    
    /**
     * The hash code is calculated from the path.
     */
    @Override public int hashCode() {
        
        int h = isAntecedent() ? -1 : 1;
        h *= termNumber;
        h ^= subtermSelector.hashCode();
        return h;
    }

    /**
     * check whether the selection refers to the antecedent side of a sequent
     * 
     * @return true, if the selection is on the antecedent soide
     */
    public boolean isAntecedent() {
        return inAntecedent;
    }

    /**
     * check whether the selection refers to the succedent side of a sequent
     * 
     * @return true, if the selection is on the succedent side
     */
    public boolean isSuccedent() {
        return !inAntecedent;
    }

    /**
     * Checks if this selection refers to a top level term
     * 
     * @return true, if the subterm number is equal to 0
     */
    public boolean isToplevel() {
        return getDepth() == 0;
    }
    
    /**
     * Gets the toplevel selector to which this selector is a subselection
     * 
     * @return a term selector which is "prefix" to this.
     */
    public@NonNull TermSelector getToplevelSelector() {
        return new TermSelector(inAntecedent, termNumber);
    }

    /**
     * If this selector selects a top level term, then this method selects a
     * subterm of the term.
     * 
     * @param subtermNo
     *            the subterm number of the term to select
     * 
     * @return a TermSelector with side and term number as this object and the
     *         subterm subtermNo
     */
    public TermSelector selectSubterm(int subtermNo) {
        return new TermSelector(this, subtermNo);
    }
    
    /**
     * Gets the number of the toplevel term to which the
     * selection refers to
     * 
     * @return the number of the term (starting at 0)
     */
    public int getTermNo() {
        return termNumber;
    }
    
    /**
     * Gets the path as list of integers.
     * 
     * @return the path to the selected subterm as unmodifiable list.
     */
    public @NonNull List<Integer> getPath() {
        return subtermSelector.getPath();
    }
    
    
    /**
     * Apply the term selector to a sequent to select a particular top level
     * term. This method ignores the path selection information.
     * 
     * <p>
     * The selection can fail, if the index is out of range.
     * 
     * @param sequent
     *            the sequent to select from
     * 
     * @return the term to which the selector refers
     * 
     * @throws ProofException
     *             if the selection cannot be applied to the sequent.
     */
    public @NonNull Term selectTopterm(@NonNull Sequent sequent) throws ProofException {
        List<Term> terms;
        if (isAntecedent()) {
            terms = sequent.getAntecedent();
        } else {
            terms = sequent.getSuccedent();
        }

        int termNo = getTermNo();
        if (termNo < 0 || termNo >= terms.size())
            throw new ProofException("Cannot select " + this + " in " + sequent);

        return terms.get(termNo);
    }

    /**
     * Apply the term selector to a sequent to select a particular term.
     * 
     * <p>
     * This method takes the term selection and the path selection information
     * into consideration.
     * 
     * <p>
     * The selection can fail, if an index (either term index or a subterm index)
     * are out of range.
     * 
     * @param sequent
     *            the sequent to select from
     * 
     * @return the term to which the selector refers
     * 
     * @throws ProofException
     *             if the selection cannot be applied to the sequent.
     */
    public @NonNull Term selectSubterm(@NonNull Sequent sequent) throws ProofException {
        Term term = selectTopterm(sequent);
        return subtermSelector.selectSubterm(term);
    }

    public SubtermSelector getSubtermSelector() {
        return subtermSelector;
    }

    
    /**
     * Checks whether this term selector has another term selector as prefix.
     * 
     * <p>
     * The result is equivalent to
     * <pre>this.toString().startsWith(prefix.toString())</pre> 
     * 
     * @param prefix
     *            the term selector which may be a prefix to this. 
     * 
     * @return true iff the argument is a prefix to this selector.
     */
    public boolean hasPrefix(@NonNull TermSelector prefix) {
        if(isAntecedent() != prefix.isAntecedent()) {
            return false;
        }

        if(getTermNo() != prefix.getTermNo()) {
            return false;
        }
        
        return getSubtermSelector().hasPrefix(prefix.getSubtermSelector());
    }
}
