/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
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
public abstract class BindableIdentifier extends Term {

    public BindableIdentifier(Term[] subterms, Type type) {
        super(subterms, type);
    }

    public BindableIdentifier(Type type) {
        super(type);
    }

	public abstract String getName();

}
