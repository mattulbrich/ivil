/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Collection;

import de.uka.iti.pseudo.term.creation.TypingContext;

public abstract class Type {

    public abstract void collectTypeVariables(Collection<String> coll);

    public abstract Type visit(TypeVisitor visitor);

}
