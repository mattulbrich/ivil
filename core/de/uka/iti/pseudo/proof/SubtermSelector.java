/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Term;


/**
 * The Class TermSelector is used to select a subterm within a term.
 * 
 * <p>
 * It does not necessarily describe a direct subterm but may describe a term at
 * arbitrary depth. It is represented by a possibly empty list of indices of
 * subterms.
 * 
 * <p>
 * The string representation of a TermSelector is a list of non-negative
 * integers separated by colons for instance <code>0.1</code> for the the
 * second subterm of the first subterm of the term to which it is applied or the
 * empty selector for the formula itself
 * 
 * <p>
 * We silently assume that no term has more than 127 subterms. This is also
 * checked by assertions.
 */
public class SubtermSelector {

    /**
     * The path of subterm numbers
     */
    private byte[] selectorInfo;
    
    /**
     * Helper class which allows to access the path as an unmodifiable list.
     */
    private class ListView extends AbstractList<Integer> {
        @Override public Integer get(int index) {
            return (int)selectorInfo[index];
        }
        @Override public int size() {
            return selectorInfo.length;
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
    public SubtermSelector(int... path) {
        this.selectorInfo = new byte[path.length];
        
        for (int i = 0; i < path.length; i++) {
            assert path[i] >= 0 && path[i] <= Byte.MAX_VALUE;
            this.selectorInfo[i] = (byte) path[i];
        }
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
    public SubtermSelector(@NonNull SubtermSelector termSelector, int subtermNo) {
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
    public SubtermSelector(@NonNull String descr) throws FormatException {
        
        if(descr.startsWith(".") || descr.endsWith("."))
            throw new FormatException("TermSelector", "Illegal character at start/end: .", descr);
        
        if(descr.length() == 0) {
            // empty selector describes the toplevel term itself
            selectorInfo = new byte[0];
            return;
        }
        
        String[] sect = descr.split("\\.");
        selectorInfo = new byte[sect.length];
        
        for (int i = 0; i < selectorInfo.length; i++) {
            try {
                if(sect[i].length() == 0)
                    throw new FormatException("SubtermSelector", "empty part", descr);
                selectorInfo[i] = Byte.parseByte(sect[i]);
                if (selectorInfo[i] < 0)
                    throw new FormatException("SubtermSelector", "negative: "
                            + sect[i], descr);
            } catch (NumberFormatException e) {
                throw new FormatException("SubtermSelector", "not a number: "
                        + sect[i], descr);
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
        for (int i = 0; i < selectorInfo.length; i++) {
            if(i > 0)
                sb.append(".");
            sb.append(selectorInfo[i]);
        }
        return sb.toString();
    }
    
    /**
     * Gets the depth of this selector. The depth is the number of sub term
     * selections needed to find the desired term. It is the length of the path,
     * 0 if the top level term itself is selected.
     * 
     * @return a number >= 0
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
        if (obj instanceof SubtermSelector) {
            SubtermSelector ts = (SubtermSelector) obj;
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
     * The hash code is calculated from the path.
     */
    @Override public int hashCode() {
        return Arrays.hashCode(selectorInfo);
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
     * 
     * @deprecated Use {@link #SubtermSelector(SubtermSelector, int)} instead
     */
    @Deprecated
    public SubtermSelector selectSubterm(int subtermNo) {
        return new SubtermSelector(this, subtermNo);
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
     * @param term
     *            the term to select in
     * 
     * @return the linear index
     * @see SubtermCollector
     */
    public int getLinearIndex(@NonNull Term term) {
        int result = 0;
        
        for (int i = 0; i < selectorInfo.length; i++) {
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
     * Apply the term selector to a term to select a particular subterm.
     * 
     * <p>
     * The selection can fail, if an index (either term index or a subterm
     * index) is out of range.
     * 
     * @param term
     *            the term to select from
     * 
     * @return the subterm of <code>term</code> specified by this selector
     * 
     * @throws ProofException
     *             if the selection cannot be applied to the term.
     */
    public Term selectSubterm(@NonNull Term term) throws ProofException {

        for (int i = 0; i < selectorInfo.length; i++) {
            byte subtermNo = selectorInfo[i];
            if(subtermNo >= term.countSubterms())
                throw new ProofException("Cannot select " + subtermNo + " in "
                        + term + " for " + this);

            term = term.getSubterm(subtermNo);
        }

        return term;
    }
    
    /**
     * Gets the subterm number which is stored in position <code>index</code>
     * in the path of this object.
     * 
     * @param index
     *            an index between 0 (incl) and {@link #getDepth()} (excl)
     * 
     * @return the subterm number in path at the given index
     * @throws IndexOutOfBoundsException
     *             if the the given index is outside the specified bounds
     */
    public int getSubtermNumber(int index) {
        return selectorInfo[index];
    }

}
