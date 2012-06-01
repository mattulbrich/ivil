/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.util.ObjectCachePool;

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
     * A constant pool is used to ensure that any term is created only once,
     * hence reusing objects in memory.
     */
    private static final ObjectCachePool TYPE_POOL = new ObjectCachePool();

    /**
     * The stored hash code.
     */
    private int storedHashCode = 0;

    /**
     * This method is the 'accept' method of the visitor pattern. It can be used
     * to call a specific method of the visitor for a certain subtype of this
     * class.
     *
     * @param visitor
     *            the visitor
     * @param parameter
     *            the parameter to the visitor
     * @param <R>
     *            the result type of the visitor
     * @param <A>
     *            the argument type of the visitor
     *
     * @return the type some result value returned by the visitor.
     *
     * @throws TermException
     *             may be thrown by the visitor
     */

    public abstract </*@Nullable*/ R, /*@Nullable*/A>
        R accept(@NonNull TypeVisitor<R,A> visitor, A parameter) throws TermException;

    /**
     * {@inheritDoc}
     *
     * <p>The string representation of a type has to ensure that if two types are equal
     * their strings are equal as well.
     */
    @Override
    public abstract @NonNull String toString();

    /**
     * {@inheritDoc}
     *
     * <p>Two types are equals if they are of the same class and structurally equal.
     */
    @Override
    public abstract boolean equals(@Nullable Object object);

    /**
     * {@inheritDoc}
     *
     * <p>
     * The hash code of a type is calculated using the string representation and
     * its hash code. Once calculated, the value is stored in the variable
     * {@link #storedHashCode}.
     */
    @Override
    public final int hashCode() {
        if(storedHashCode == 0) {
            storedHashCode = toString().hashCode();
        }
        return storedHashCode;
    }

    /**
     * Gets the canonical representative for all types which are equal to this.
     *
     * @return a type which is {@linkplain Object#equals(Object) equal} to this
     *         type object and of exactly the same class.
     */
    protected @NonNull Type intern() {
        return TYPE_POOL.cacheNonNull(this);
    }

}
