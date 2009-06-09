/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

/**
 * The Class CompoundModality captures serial composition of program modalities.
 */
public class CompoundModality extends Modality {
    
    /**
     * Instantiates a new compound modality.
     * 
     * @param mod1
     *            the first modality (left of ;)
     * @param mod2
     *            the second modality (right of ;)
     */
    public CompoundModality(Modality mod1, Modality mod2) {
        super(mod1, mod2);
    }

    @Override
    public String toString(boolean typed) {
        return getSubModality(0).toString(typed) + "; " + getSubModality(1).toString(typed);
    }

    @Override
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /*
     * This object is equal to another object if it is a Compound modality
     * and both parts are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof CompoundModality) {
            CompoundModality com = (CompoundModality) object;
            return com.getSubModality(0).equals(getSubModality(0))
                    && com.getSubModality(1).equals(getSubModality(1));
        }
        return false;
    }

}
