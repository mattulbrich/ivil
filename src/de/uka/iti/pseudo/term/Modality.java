/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

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
    
    public abstract String toString(boolean typed);

}
