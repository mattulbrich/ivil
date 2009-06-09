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

// TODO DOC

public abstract class Modality {
    
    private Modality subModality[];
    
    protected Modality(Modality... subModality) {
        this.subModality = subModality;
    }
    
    public String toString() {
        return toString(false);
    }
    
    public Modality getSubModality(int i) {
        return subModality[i];
    }
    
    public int countModalities() {
        return subModality.length;
    }
    
    public List<Modality> getSubModalities() {
        return Util.readOnlyArrayList(subModality);
    }
    
    public abstract void visit(ModalityVisitor visitor) throws TermException;
    
    public abstract String toString(boolean typed);

    @Override
    public abstract boolean equals(@NonNull Object object);
    
}
