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

package de.uka.iti.pseudo.util;

import java.util.Observable;

/**
 * The pair class can be used to combine two objects to one.
 * 
 * This is helpful when a method or declaration expects one object only but one
 * wants to provide two. For instance as target type in maps or as arguments to
 * {@link Observable#notifyObservers(Object)}.
 * 
 * A pair is immutable.
 * 
 * @param <E>
 *            Type of the first component
 * @param <F>
 *            Type of the second component
 */

public class Pair<E,F> {
	
	/**
     * the object at the first component.
     * This is the object that has been provided to the constructor as first argument.
     */
	private E fstComponent;
	
	/**
     * the object at the second component.
     * This is the object that has been provided to the constructor as second argument.
     */
	private F sndComponent;
	
	/**
     * Instantiate a new pair.
     * 
     * @param fst
     *            the first component
     * @param snd
     *            the second component
     */
    public Pair(E fst, F snd) {
        super();
        this.fstComponent = fst;
        this.sndComponent = snd;
    }

    /**
     * Alternative way to create a pair. Java is able to infer type arguments
     * for methods (like this) but not for constructors. It is therefore easier
     * to use this
     * 
     * @param fst
     *            the first component
     * @param snd
     *            the second component
     * 
     * @param <E>
     *            Type of the first component
     * @param <F>
     *            Type of the second component
     * 
     * @return a freshly created pair
     */
    public static <E,F> Pair<E,F> make(E fst, F snd) {
        return new Pair<E, F>(fst, snd);
    }

	/**
     * get the object at the first component.
     * This is the object that has been provided to the constructor as first argument.
     * 
     * @return the stored object, may be null
     */
	public E fst() {
		return fstComponent;
	}
	
	/**
     * get the object at the second component.
     * This is the object that has been provided to the constructor as second argument.
     * 
     * @return the stored object, may be null
     */
	public F snd() {
		return sndComponent;
	}

	/**
     * A pair is equal to another object if it is a pair and the components are
     * equal to one another (or both null)
     * 
     * The type parametrisation does not need to coincide.
     * 
     * @param obj
     *            object to test equality against.
     */
	public boolean equals(Object obj) {
		if (obj instanceof Pair<?,?>) {
			Pair<?,?> pair = (Pair<?,?>) obj;
			return (fst() == null ? pair.fst() == null : fst().equals(pair.fst())) &&
				(snd() == null ? pair.snd() == null : snd().equals(pair.snd()));
		} else {
			return false;
		}
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return "Pair[" + fst() + "," + snd() + "]";
	}
	
	/**
     * {@inheritDoc}
     * 
     * The hash code of a pair is the exclusive or of the hashcode of the first
     * and second component
     */
	@Override public int hashCode() {
	    int h1 = fst() == null ? 0 : fst().hashCode();
	    int h2 = snd() == null ? 0 : snd().hashCode();
	    return h1 ^ h2;
	}
}
