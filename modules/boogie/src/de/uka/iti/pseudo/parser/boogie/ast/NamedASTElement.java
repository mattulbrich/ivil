/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.ast;

public interface NamedASTElement {

    /**
     * @return the name of this element, that allows to identify it uniquely if
     *         the type of the element is known
     */
    public String getName();
}
