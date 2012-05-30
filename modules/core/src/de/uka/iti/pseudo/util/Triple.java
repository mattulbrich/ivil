/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.util;

import nonnull.Nullable;

/**
 * The Triple class can be used to combine three objects to one.
 *
 * This is helpful when a method or declaration expects one object only but one
 * wants to provide three. For instance as target type in maps or as arguments
 * to {@link java.util.Observable#notifyObservers(Object)}.
 *
 * <p>
 * A triple is immutable.
 *
 * @param <E>
 *            the generic type parameter for the first component
 * @param <F>
 *            the generic type parameter for the second component
 * @param <G>
 *            the generic type parameter for the third component
 */

public class Triple<E,F,G> {

    /**
     * the object at the first component.
     * This is the object that has been provided to the constructor as first argument.
     */
    private final E fstComponent;

    /**
     * the object at the second component.
     * This is the object that has been provided to the constructor as second argument.
     */
    private final F sndComponent;

    /**
     * the object at the third component.
     * This is the object that has been provided to the constructor as third argument.
     */
    private final G trdComponent;

    /**
     * Instantiate a new triple.
     *
     * @param fst
     *            the first component
     * @param snd
     *            the second component
     * @param trd
     *            the third component
     */
    public Triple(E fst, F snd, G trd) {
        super();
        this.fstComponent = fst;
        this.sndComponent = snd;
        this.trdComponent = trd;
    }

    /**
     * Alternative way to create a triple. Java is able to infer type arguments
     * for methods (like this) but not for constructors. It is therefore easier
     * to use this
     *
     * @param fst
     *            the first component
     * @param snd
     *            the second component
     * @param trd
     *            the third component
     * @param <E>
     *            the generic type parameter for the first component
     * @param <F>
     *            the generic type parameter for the second component
     * @param <G>
     *            the generic type parameter for the third component
     * @return a freshle created pair
     */
    public static <E,F,G> Triple<E,F,G> make(E fst, F snd, G trd) {
        return new Triple<E, F, G>(fst, snd, trd);
    }

    /**
     * get the object at the first component.
     * This is the object that has been provided to the constructor as first argument.
     *
     * @return the stored object, may be null
     */
    public @Nullable E fst() {
        return fstComponent;
    }

    /**
     * get the object at the second component.
     * This is the object that has been provided to the constructor as second argument.
     *
     * @return the stored object, may be null
     */
    public @Nullable F snd() {
        return sndComponent;
    }

    /**
     * get the object at the second component.
     * This is the object that has been provided to the constructor as third argument.
     *
     * @return the stored object, may be null
     */
    public @Nullable G trd() {
        return trdComponent;
    }

    /**
     * A triple is equal to another object if it is a triple and the components
     * are equal to one another (or both null)
     *
     * The type parametrisation does not need to coincide.
     *
     * @param obj
     *            object to test equality against.
     *
     * @return <code>true</code> if and only if the given object is a triple and
     *         all three components are {@linkplain Object#equals(Object) equal}.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Triple<?,?,?>) {
            Triple<?,?,?> triple = (Triple<?,?,?>) obj;
            return (fst() == null ? triple.fst() == null : fst().equals(triple.fst())) &&
                    (snd() == null ? triple.snd() == null : snd().equals(triple.snd())) &&
                    (trd() == null ? triple.trd() == null : trd().equals(triple.trd()));
        } else {
            return false;
        }
    }



    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // Checkstyle: IGNORE MultipleStringLiterals
        return "Triple[" + fst() + "," + snd() + "," + trd() + "]";
    }

    /**
     * {@inheritDoc}
     *
     * The hash code of a triple is the exclusive or of the hashcode of the first,
     * second, and third component.
     */
    @Override public int hashCode() {
        int h1 = fst() == null ? 0 : fst().hashCode();
        int h2 = snd() == null ? 0 : snd().hashCode();
        int h3 = trd() == null ? 0 : trd().hashCode();
        return h1 ^ h2 ^ h3;
    }
}
