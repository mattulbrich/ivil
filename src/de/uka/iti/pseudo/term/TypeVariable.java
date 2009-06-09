/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Collection;

/*
 * Special type variables
 * 
 * '#<id> used by TypeUnification to make variants
 * '%<id> type of a schema variable
 * '%%<id> type of a schema identifier
 * '[0-9]+ temporily created types during type inference
 * '<id> usual type variables, entered by user
 */

public class TypeVariable extends Type {

    private String typeVar;
    
    /* to distinguish from normal type variables, they can have same name */
    private boolean formal;

    public TypeVariable(String typeVar) {
    	this(typeVar, false);
    }
    
    public TypeVariable(String typeVar, boolean formal) {
        this.typeVar = typeVar;
        this.formal = formal;
	}

	@Override
    public void collectTypeVariables(Collection<String> coll) {
        coll.add(typeVar);
    }

	@Override
	public String toString() {
		return "'" + getVariableName();
	}
	
    public String getVariableName() {
        return typeVar; 
    }

	public boolean isFormal() {
		return formal;
	}
	
	@Override
	public Type visit(TypeVisitor visitor) throws TermException {
	    return visitor.visit(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeVariable) {
			TypeVariable tyv = (TypeVariable) obj;
			return typeVar.equals(tyv.typeVar);
		}
		return false;
	}

}
