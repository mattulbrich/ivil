/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.util.Util;

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
	
	// to enforce overriding
	public abstract String toString();
	
	protected abstract void visit(TermVisitor visitor);

	
}
