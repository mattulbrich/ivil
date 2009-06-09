/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;

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

	@Override public void visit(TermVisitor visitor) throws TermException {
		visitor.visit(this);
	}
	
	public String getName() {
		return name;
	}

    @Override
    public boolean equals(@NonNull Object object) {
        if (object instanceof Variable) {
            Variable v = (Variable) object;
            return v.getName().equals(getName())
                    && getType().equals(v.getType());
        }
        return false;
    }

}
