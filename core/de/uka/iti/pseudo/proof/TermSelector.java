/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.AbstractList;
import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;

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
     * The path includes also the term number
     * and always has a length > 0.
     */
    private byte[] selectorInfo;
    
    /**
     * Helper class which allows to access the path as an unmodifiable list.
     */
    private class ListView extends AbstractList<Integer> {
        @Override public Integer get(int index) {
            return (int)selectorInfo[index + 1];
        }
        @Override public int size() {
            return selectorInfo.length - 1;
        }
    }

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
        this.inAntecedent = inAntecedent;
        
        assert termNo >= 0 && termNo <= Byte.MAX_VALUE;

        this.selectorInfo = new byte[path.length + 1];
        
        for (int i = 0; i < path.length; i++) {
            assert path[i] >= 0 && path[i] <= Byte.MAX_VALUE;
            this.selectorInfo[i+1] = (byte) path[i];
        }
        
        this.selectorInfo[0] = (byte) termNo;
    }

    /**
     * Instantiates a new term selector from a term selector and a subterm
     * number.
     * 
     * <p>
     * The created selector contains the same information as the argument.
     * Only the path to the subterm is augmented by subtermNo.
     * 
     * @param termSelector
     *            the term selector to modify
     * @param subtermNo
     *            the subterm number to select
     */
    public TermSelector(TermSelector termSelector, int subtermNo) {
        this.inAntecedent = termSelector.inAntecedent;
        
        assert subtermNo >= 0 && subtermNo <= Byte.MAX_VALUE;

        selectorInfo = new byte[termSelector.selectorInfo.length + 1];
        for (int i = 0; i < termSelector.selectorInfo.length; i++) {
            this.selectorInfo[i] = termSelector.selectorInfo[i];
        }
        selectorInfo[selectorInfo.length-1] = (byte) subtermNo;
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
        
        if(descr.startsWith(".") || descr.endsWith("."))
            throw new FormatException("TermSelector", "Illegal character at start/end: .", descr);
        
        String[] sect = descr.split("\\.");

        if ("A".equals(sect[0])) {
            inAntecedent = true;
        } else if ("S".equals(sect[0])) {
            inAntecedent = false;
        } else
            throw new FormatException("TermSelector", "unknown first part: "
                    + sect[0], descr);

        selectorInfo = new byte[sect.length - 1];
        
        if(selectorInfo.length < 1) {
            throw new FormatException("TermSelector", "insufficient information, top level formula needed", descr);
        }

        for (int i = 0; i < selectorInfo.length; i++) {
            try {
                if(sect[i+1].length() == 0)
                    throw new FormatException("TermSelector", "empty part", descr);
                selectorInfo[i] = Byte.parseByte(sect[i+1]);
                if (selectorInfo[i] < 0)
                    throw new FormatException("TermSelector", "negative: "
                            + sect[i+1], descr);
            } catch (NumberFormatException e) {
                throw new FormatException("TermSelector", "not a number: "
                        + sect[i+1], descr);
            }
        }
    }

    /**
     * a string representation of the TermSelector. We create an equal object if
     * parsing this string using {@link TermSelector#TermSelector(String)}.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(inAntecedent ? "A" : "S");
        for (byte p : selectorInfo) {
            sb.append(".").append(p);
        }
        return sb.toString();
    }
    
    /**
     * Gets the depth of this selector.
     * The depth is the number of term selections needed to find the desired term.
     * It is the length of the path plus one (for selecting the top level term)
     * 
     * @return a number >= 1
     */
    public int getDepth() {
        return selectorInfo.length;
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
            
            if(getDepth() != getDepth())
                return false;
            
            for (int i = 0; i < selectorInfo.length; i++) {
                if(selectorInfo[i] != ts.selectorInfo[i])
                    return false;
            }
            
            return true;
        }
        return false;
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
     * @return true, if the selection is on the succedent soide
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
        return getDepth() == 1;
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
        return selectorInfo[0];
    }
    
    /**
     * Gets the path as list of integers.
     * 
     * @return the path to the selected subterm as unmodifiable list.
     */
    public List<Integer> getPath() {
        return new ListView();
    }
    
    
    /**
     * Gets the linear index of a subterm.
     * 
     * If enumerating all subterms in an infix tree visiting fashion, this index
     * would result in the selected term. SubtermCollector provides such a
     * linear list for instance.
     * 
     * Sequent side and term number are ignored here, only the path is taken into
     * consideration
     * 
     * @param term
     *            the term to select in
     * 
     * @return the linear index
     * @see SubtermCollector
     */
    public int getLinearIndex(@NonNull Term term) {
        int result = 0;
        
        for (int i = 1; i < selectorInfo.length; i++) {
            int v = selectorInfo[i];
            for(int j = 0; j < v; j++) {
                result += countAllSubterms(term.getSubterm(j));
            }
            result ++;
            term = term.getSubterm(v);
        }
        return result;
    }
    
    /* needed fot getLinearIndex */
    private int countAllSubterms(Term term) {
        // count myself as well
        int result = 1;
        for (int i = 0; i < term.countSubterms(); i++) {
            result += countAllSubterms(term.getSubterm(i));
        }
        
        return result;
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
    public Term selectSubterm(@NonNull Sequent sequent) throws ProofException {
        Term term = selectTopterm(sequent);
        return selectSubterm(term);
    }
    
    
    /**
     * Apply the term selector to a term to select a particular subterm.
     * 
     * <p>This method does not take antecedent/succedent or term number into account.
     * It only returns the subterm denoted by the selection path within the term.
     * <p>
     * The selection can fail, if an index (either term index or a subterm index)
     * are out of range.
     * 
     * @param term
     *            the term to select from
     * 
     * @return the subterm of term selected by the path 
     * 
     * @throws ProofException
     *             if the selection cannot be applied to the term.
     */
    public Term selectSubterm(@NonNull Term term) throws ProofException {

        for (int i = 1; i < selectorInfo.length; i++) {
            byte subtermNo = selectorInfo[i];
            if(subtermNo >= term.countSubterms())
                throw new ProofException("Cannot select " + subtermNo + " in "
                        + term + " for " + this);

            term = term.getSubterm(subtermNo);
        }

        return term;
    }

}
