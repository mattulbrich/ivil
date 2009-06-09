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
 * The Class SkipModality captures a modality that does not change the 
 * interpretation of anything
 */
public class SkipModality extends Modality {

    /**
     * {@inheritDoc}
     * 
     * <p>In either case this is the word <code>skip</code> here.
     * @param typed irrelevant
     * @return <code>skip</code>
     */
    @Override
    public String toString(boolean typed) {
        return "skip";
    }

    @Override
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    @Override 
    public boolean equals(Object object) {
        return object instanceof SkipModality;
    }

}
