/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

public class Variable extends Term {
	
	private String name;

	public Variable(String name, Type type) {
		super(type);
		this.name = name;
	}
	
	@Override
	public String toString(boolean typed) {
		String retval = name;
		if(typed)
		    retval += " as " + getType();
		return retval;
	}

	@Override
	protected void visit(TermVisitor visitor) {
		visitor.visit(this);
	}
	
	public String getName() {
		return name;
	}

}
