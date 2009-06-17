/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.List;

import nonnull.NonNull;

import de.uka.iti.pseudo.util.Util;

/**
 * The Class Term is the base class for the term data structure.
 * 
 * It stores the subterms of the term along with its type
 * and provides means for visitation by a {@link TermVisitor}.
 */
@NonNull
public abstract class Term {
	
	/**
	 * The Constant NO_ARGUMENTS is used instead of null
	 * when a term has no subterms
	 */
	private static final Term[] NO_ARGUMENTS = new Term[0];
	
	/**
	 * The Constant SHOW_TYPES is read from the 
	 * system environment and indicates whether terms are
	 * to be output with or without types.
	 */
	public static boolean SHOW_TYPES = Boolean.getBoolean("pseudo.showtypes");
	
	/**
	 * The array of sub terms
	 */
	private Term[] subterms;
	
	/**
	 * The type of this term.
	 */
	private Type type;
    
    /**
     * The hashcode is stored in here once it has been calculated.
     */
    private int storedHashCode;

	/**
	 * Instantiates a new term with subterms
	 * 
	 * @param subterms the subterms
	 * @param type the type of the term
	 */
	protected Term(@NonNull Term[] subterms, @NonNull Type type) {
		
		assert subterms != null;
		assert type != null;
		
		this.subterms = subterms;
		this.type = type;
	}
	
	/**
	 * Instantiates a new term without subterms
	 * 
	 * @param type the type of the term
	 */
	protected Term(@NonNull Type type) {
		this(NO_ARGUMENTS, type);
	}

	/**
	 * Gets the type of the term
	 * 
	 * @return type of this term
	 */
	public @NonNull Type getType() {
		return type;
	}

	/**
	 * Count the subterms of term
	 * 
	 * @return a non-negative number
	 */
	public int countSubterms() {
		return subterms.length;
	}
	
	/**
	 * Gets a particular subterm.
	 * 
	 * @param idx the index of the subterm
	 * 
	 * @return the subterm
	 */
	public Term getSubterm(int idx) {
		return subterms[idx];
	}
	
	/**
	 * Gets the subterms as an unmodifiable list.
	 * 
	 * @return the subterms as list
	 */
	public List<Term> getSubterms() {
		return Util.readOnlyArrayList(subterms);
	}
	
	/**
	 * depending on {@link #SHOW_TYPES} print the term
	 * woth or without typing information
	 * 
	 * @return string for this term
	 */
	public String toString() {
	    return toString(SHOW_TYPES);
	}
	
	/**
	 * Depending on the argument typed print the term with or 
	 * without typing information.
	 * 
	 * If typing is switched on, every subterm will be annotated
	 * as in "t as T". 
	 * 
	 * @param typed should the result contain typing information 
	 * 
	 * @return the string for this term, with tpying information
	 *         iff typed==true.
	 */
	public abstract String toString(boolean typed);
	
	/**
	 * {@inheritDoc}
	 * 
	 * The hash code of a term is calculated using its string
	 * representation. Its result is cached in a field so that
	 * the calculation does not need to happen a second time
	 * 
	 * Please note that terms which are not equal may have the same 
	 * string representation and therefore the same hash code.
	 * 
	 * @return the hash code for this 
	 */
	@Override 
	public int hashCode() {
	    if(storedHashCode == 0) {
	        storedHashCode = toString(true).hashCode();
	    }
	    return storedHashCode;
	}
	
	/**
     * The equality on terms is the syntactical identity.
     * 
     * Two terms are equal if and only if they are structually the same
     * including their types, that is:<br>
     * 
     * <code>nil as List(int)</code> is not equal to
     * <code>nil as List('a)</code> even if 'a might be instantiated to int.
     * 
     * @param object
     *            an arbitrary object
     * 
     * @return true iff object is a term and structually equal to this term.
     */
	public abstract boolean equals(@NonNull Object object);
	
	/**
	 * This is the "accept" method of the visitor pattern.
	 * 
	 * @param visitor the visitor to accept
	 * 
	 * @throws TermException may be thrown by the visit method of the visitor.
	 */
	public abstract void visit(TermVisitor visitor) throws TermException;

	
}
