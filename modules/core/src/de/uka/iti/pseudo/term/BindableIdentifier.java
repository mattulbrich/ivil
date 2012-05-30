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

/**
 * The Class BindableIdentifier models a special kind of
 * term which can be bound using a binder construct.
 * This is the case for {@link Variable}s and {@link SchemaVariable}s.
 */
public abstract class BindableIdentifier extends Term implements Comparable<BindableIdentifier> {

    public BindableIdentifier(Term[] subterms, Type type) {
        super(subterms, type);
    }

    public BindableIdentifier(Type type) {
        super(type);
    }

    public abstract String getName();

    /**
     * {@inheritDoc}
     * 
     * <p>Comparison is performed on the string representation of the bindable identifiers.
     */
    @Override
    public int compareTo(BindableIdentifier o) {
        return toString(true).compareTo(o.toString(true));
    }
}
