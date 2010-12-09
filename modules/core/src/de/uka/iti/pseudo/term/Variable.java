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
package de.uka.iti.pseudo.term;

import nonnull.Nullable;

/**
 * The Class Variable captures a bound variable in a term
 */
public class Variable extends BindableIdentifier {
	
	/**
	 * The name of the bound variable
	 */
	private String name;

	/**
	 * Instantiates a new variable.
	 * 
	 * @param name the name of the variable
	 * @param type the type of the variable
	 */
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

	@Override public void visit(TermVisitor visitor) throws TermException {
		visitor.visit(this);
	}
	
	/**
	 * Gets the name of the variable
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof Variable) {
            Variable v = (Variable) object;
            return v.getName().equals(getName())
                    && getType().equals(v.getType());
        }
        return false;
    }

}
