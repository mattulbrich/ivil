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

/**
 * The Class Type is used to model types of terms. Types are immutable objects.
 * 
 * <p>
 * This abstract superclass provides an infrastructure for caching calculated
 * hash values.
 * 
 * @see TypeVariable
 * @see TypeApplication
 * @see SchemaType
 * 
 * @author mattias ulbrich
 */
public abstract class Type {

    /**
     * The stored hash code.
     */
    private int storedHashCode;

    /**
     * This method is the 'accept' method of the visitor pattern. It can be used
     * to call a specific method of the visitpr for a certain subtype of this
     * class.
     * 
     * @param visitor
     *            the visitor
     * 
     * @return the type some result value returned by the visitor.
     * 
     * @throws TermException
     *             may be thrown by the visitor
     */
    public abstract <R,A> R accept(@NonNull TypeVisitor<R,A> visitor, A parameter) throws TermException;
    
    /**
     * {@inheritDoc}
     * 
     * <p>The string representation of a type has to ensure that if two types are equal
     * there strings are equal as well.
     */
    @Override
    public abstract @NonNull String toString();
    
    /**
     * {@inheritDoc}
     * 
     * <p>Two types are equals if they are of the same class and structurally equal.
     */
    @Override
    public abstract boolean equals(@NonNull Object object);
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * The hash code of a type is calculated using the string representation and
     * its hash code. Once calculated, the value is stored in the variable
     * {@link #storedHashCode}.
     */
    @Override 
    public int hashCode() {
        if(storedHashCode == 0) {
            storedHashCode = toString().hashCode();
        }
        return storedHashCode;
    }

    

}
