/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.util;

import java.util.Observable;

/**
 * The Triple class can be used to combine three objects to one.
 * 
 * This is helpful when a method or declaration expects one object only but one
 * wants to provide three. For instance as target type in maps or as arguments to
 * {@link Observable#notifyObservers(Object)}.
 * 
 * A triple is immutable.
 */

public class Triple<E,F,G> {
	
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
     * the object at the third component.
     * This is the object that has been provided to the constructor as third argument.
     */
    private G trdComponent;
	
	/**
     * Instantiate a new pair.
     * 
     * @param fst
     *            the first component
     * @param snd
     *            the second component
     *            @param trd
     *            the third component
     */
    public Triple(E fst, F snd, G trd) {
        super();
        this.fstComponent = fst;
        this.sndComponent = snd;
        this.trdComponent = trd;
    }
    
    /**
     * Alternative way to create a triple. Java is able to infer type arguments for
     * methods (like this) but not for constructors. It is therefore easier to 
     * use this
     * 
     * @param fst
     *            the first component
     * @param snd
     *            the second component
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
     * get the object at the second component.
     * This is the object that has been provided to the constructor as third argument.
     * 
     * @return the stored object, may be null
     */
    public G trd() {
        return trdComponent;
    }

	/**
     * A triple is equal to another object if it is a triple and the components are
     * equal to one another (or both null)
     * 
     * The type parametrisation does not need to coincide.
     * 
     * @param obj
     *            object to test equality against.
     */
	public boolean equals(Object obj) {
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
