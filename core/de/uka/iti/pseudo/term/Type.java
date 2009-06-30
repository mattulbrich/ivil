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

// TODO DOC

public abstract class Type {

    private int storedHashCode;

    public abstract Type visit(TypeVisitor visitor) throws TermException;
    
    @Override
    public abstract @NonNull String toString();
    
    @Override
    public abstract boolean equals(@NonNull Object object);
    
    @Override 
    public int hashCode() {
        if(storedHashCode == 0) {
            storedHashCode = toString().hashCode();
        }
        return storedHashCode;
    }

    

}
