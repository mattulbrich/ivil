/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * This class encapsulates a type variable type with an arbitrary name.
 * 
 * <p>
 * All type variables are printed prefixed with a prime symbol. <b>Their name,
 * however, does not include that prime.</b>
 * 
 * <p>
 * Type variables stand for one ordinary type (variable free expression over the
 * type constructors) in one interpretation. They are not meant to be
 * instantiated. SchemaTypeVariables may be instantiated.
 *
 * @see TypeApplication
 * @see SchemaType
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
	public <R,A> R accept(TypeVisitor<R,A> visitor, A parameter) throws TermException {
	    return visitor.visit(this, parameter);
	}
	
	/** 
	 * Two type variables are equal iff their names are equal.
	 */
	@Override
	public boolean equals(@Nullable Object obj) {
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
