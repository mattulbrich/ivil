/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;

/**
 * This class implements a depth visitor for types. For type applications the
 * visitor is applied to all arguments.
 *
 * @param <A>
 *            The type of the additional argument passed to the visit methods.
 */
@SuppressWarnings("nullness")
public class DefaultTypeVisitor<A> implements TypeVisitor<Void, A> {

    @Override
    public Void visit(TypeApplication typeApplication, A argument)
            throws TermException {
        typeApplication.acceptDeep(this, argument);
        return null;
    }

    @Override
    public Void visit(TypeVariable typeVariable, A argument)
            throws TermException {
        return null;
    }

    @Override
    public Void visit(SchemaType schemaTypeVariable, A argument)
            throws TermException {
        return null;
    }
}
