/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

// TODO DOC

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

    /** 
     * a predefined type variable for convenience 
     */
    public final static TypeVariable ALPHA = new TypeVariable("a");
    
    /** 
     * a second predefined type variable for convenience 
     */
    public final static TypeVariable BETA = new TypeVariable("b");
    
    public static final String VARIANT_PREFIX = "#";
    
    private String typeVar;
    
    public TypeVariable(String typeVar) {
    	this.typeVar = typeVar;
    }
    
	@Override
	public String toString() {
		return "'" + getVariableName();
	}
	
    public String getVariableName() {
        return typeVar; 
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
	
	@Override
	public int hashCode() {
		return typeVar.hashCode();
	}

}
