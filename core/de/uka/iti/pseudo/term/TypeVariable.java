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

import nonnull.NonNull;
import de.uka.iti.pseudo.term.creation.TypeUnification;

/*
 * Special type variables
 * 
 * '#<id> used by TypeUnification to make variants
 * '%<id> type of a schema variable
 * '%%<id> type of a schema identifier
 * '[0-9]+ temporily created types during type inference
 * '<id> usual type variables, entered by user
 */

/**
 * This class encapsulates a type variable type with an arbitrary name.
 * 
 * <p>
 * All type variables are printed prefixed with a prime symbol. Their name,
 * however, does not include that prime.
 * 
 * <h4>Special type variables</h4>
 * <ul>
 * <li><code>{@literal '#<id>}</code>: used by TypeUnification to 
 * make uninstantiatable variants
 * <li><code>{@literal '%<id>}</code>: type of a schema variables
 * <li><code>{@literal '[0-9]+}</code>: temporily created types during type inference
 * <li><code>{@literal '<id>}</code>: usual type variables, entered by user
 * </ul>
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
    
    /**
     * The prefix used to indicate that a type variable cannot be instantiated.
     * @see TypeUnification
     */
    public static final String VARIANT_PREFIX = "#";
    
    /**
     * The actual name (w/o leading ')
     */
    private String name;
    
    /**
     * Instantiates a new type variable.
     * 
     * @param typeVar
     *            the name of the type variables (without leading ')
     */
    public TypeVariable(@NonNull String typeVar) {
    	this.name = typeVar;
    }

    /**
     * A type variable is rendered to a string by prepending a prime ' to its
     * name.
     */
	@Override
	public String toString() {
		return "'" + getVariableName();
	}
	
    /**
     * Gets the variable name w/o the leading prime '.
     * 
     * @return the variable name
     */
    public @NonNull String getVariableName() {
        return name; 
    }

	/* (non-Javadoc)
	 * @see de.uka.iti.pseudo.term.Type#visit(de.uka.iti.pseudo.term.TypeVisitor)
	 */
	@Override
	public Type visit(TypeVisitor visitor) throws TermException {
	    return visitor.visit(this);
	}
	
	/** 
	 * Two type variables are equal iff their names are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeVariable) {
			TypeVariable tyv = (TypeVariable) obj;
			return name.equals(tyv.name);
		}
		return false;
	}
	
	/**
	 * the hashcode of a type variable is the hash code of its name.S
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
