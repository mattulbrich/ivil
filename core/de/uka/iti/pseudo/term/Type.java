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

/**
 * The Class Type is used to model types of terms. Types are immutable objects.
 * 
 * <p>
 * This abstract superclass provides an infrastructure for caching calculated
 * hash values.
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
    public abstract Type visit(@NonNull TypeVisitor visitor) throws TermException;
    
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
     * The hashcode of a type is calculated using the string representation and
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
