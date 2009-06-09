/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.List;

import nonnull.NonNull;

import de.uka.iti.pseudo.util.Util;

/**
 * The Class Modality is the base class of all modality elements within terms.
 * 
 * Modalities can have submodalities and can be visited using a
 * {@link ModalityVisitor}.
 */
public abstract class Modality {

    /**
     * The submodality.
     */
    private Modality subModality[];

    /**
     * The stored hash code which is lazily created.
     */
    private int storedHashCode;

    /**
     * Instantiates a new modality.
     * 
     * @param subModality
     *            the sub modality
     */
    protected Modality(Modality... subModality) {
        this.subModality = subModality;
    }

    /**
     * return a string rpresentation. Whether or not types are printed is
     * determined by {@link Term#SHOW_TYPES}.
     * 
     * @return string representation of this modality.
     */
    public String toString() {
        return toString(Term.SHOW_TYPES);
    }

    /**
     * Gets one submodality.
     * 
     * @param i
     *            the index of the submodality
     * 
     * @return the submodality
     * 
     * @throws IndexOutOfBoundsException
     *             if the index is out of the bounds of the list of
     *             submodalities.
     */
    public Modality getSubModality(int i) throws IndexOutOfBoundsException {
        return subModality[i];
    }

    /**
     * Count the sub modalities.
     * 
     * @return the number of submodalities
     */
    public int countModalities() {

        return subModality.length;
    }

    /**
     * Gets the sub modalities as an unmodifiable list.
     * 
     * @return the sub modalities as list
     */
    public List<Modality> getSubModalities() {
        return Util.readOnlyArrayList(subModality);
    }

    /**
     * calculate the hash code as hashcode of the string representation. the
     * result is stored in a field. The second and following calls do not need
     * to calculate again
     * 
     * @return the hashcode which is the same for all modalities which are
     *         equal.
     */
    @Override public int hashCode() {
        if (storedHashCode == 0) {
            storedHashCode = toString(true).hashCode();
        }
        return storedHashCode;
    }

    /**
     * The "accept" method of the visitor pattern.
     * 
     * Every subclass calls the appropriate visit method
     * 
     * @param visitor
     *            the visitor to call back
     * 
     * @throws TermException
     *             may be thrown by the visitor
     */
    public abstract void visit(ModalityVisitor visitor) throws TermException;

    /**
     * Get a string representation of this modality with or without explicit
     * types.
     * 
     * @param typed
     *            if true, every subterm is explicitly typed (with "as type")
     * 
     * @return the string representation
     */
    public abstract String toString(boolean typed);

    @Override public abstract boolean equals(@NonNull Object object);

}
