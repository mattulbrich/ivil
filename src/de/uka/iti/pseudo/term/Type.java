/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;

public abstract class Type {

    public abstract Type visit(TypeVisitor visitor) throws TermException;
    
    @Override
    public abstract @NonNull String toString();
    
    @Override
    public abstract boolean equals(@NonNull Object object);

}
