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

@NonNull
public abstract class Term {
	
	private static final Term[] NO_ARGUMENTS = new Term[0];
	
	private Term[] subterms;
	
	private Type type;

	protected Term(Term[] subterms, Type type) {
		
		assert subterms != null;
		assert type != null;
		
		this.subterms = subterms;
		this.type = type;
	}
	
	public Term(Type type) {
		this(NO_ARGUMENTS, type);
	}

	public Type getType() {
		return type;
	}

	public int countSubterms() {
		return subterms.length;
	}
	
	public Term getSubterm(int i) {
		return subterms[i];
	}
	
	public List<Term> getSubterms() {
		return Util.readOnlyArrayList(subterms);
	}
	
	public String toString() {
	    return toString(false);
	}
	
	public abstract String toString(boolean typed);
	
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
	
	public abstract void visit(TermVisitor visitor) throws TermException;

	
}
