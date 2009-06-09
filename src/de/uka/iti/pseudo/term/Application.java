/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Function;

public class Application extends Term {
	
	private Function function;

	public Application(Function funct, Type type, Term[] subterms) {
		super(subterms, type);
		this.function = funct;
		// TODO type checking, arity checking
	}

	public Application(Function funct, Type type) {
		super(type);
		this.function = funct;
		// TODO type checking, arity checking
	}

	@Override
	public String toString() {
		String retval = function.getName();
		if (countSubterms() > 0) {
			retval += "(";
			for (int i = 0; i < countSubterms(); i++) {
				retval += getSubterm(i);
				if (i != countSubterms() - 1)
					retval += ",";
			}
			retval += ")";
		}
		return retval;
	}
	
	@Override
	protected void visit(TermVisitor visitor) {
		visitor.visit(this);
	}


}
