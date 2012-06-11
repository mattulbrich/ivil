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


/**
 * The Class BindableIdentifier is the superclass to the term which can be bound
 * using a binder construct. This is the case for {@link Variable}s and
 * {@link SchemaVariable}s.
 *
 * {@link BindableIdentifier}s can be compared with one another.
 *
 * @see Variable
 * @see SchemaVariable
 * @see Binding
 */
public abstract class BindableIdentifier extends Term implements Comparable<BindableIdentifier> {

    /**
     * Instantiates a new bindable identifier.
     *
     * @param type
     *            the type of the new identitier
     */
    protected BindableIdentifier(@NonNull Type type) {
        super(type);
    }

    /**
     * Gets the name of this bindable identifier.
     *
     * @return the unique name of this bindable.
     */
    public abstract @NonNull String getName();

    /**
     * {@inheritDoc}
     *
     * <p>
     * Comparison is performed on the string representation of the bindable
     * identifiers.
     */
    @Override
    public int compareTo(BindableIdentifier o) {
        return toString(true).compareTo(o.toString(true));
    }
}
