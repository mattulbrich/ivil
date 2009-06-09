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

public class ModalityTerm extends Term {

	private Modality modality; 

	public ModalityTerm(Modality modality, Term subterm) {
		super(new Term[] { subterm }, subterm.getType());
		this.modality = modality;
	}

	@Override
	public void visit(TermVisitor visitor) throws TermException {
		visitor.visit(this);
	}

	@Override
	public String toString(boolean typed) {
	    String m = modality.toString(typed);
	    String t = getSubterm(0).toString(typed);
	    if(typed)
	        return "[" + m + "](" + t + ")";
	    else
	        return "[" + m + "]" + t;
	}

    public Modality getModality() {
        return modality;
    }

    @Override 
    public boolean equals(@NonNull Object object) {
        if (object instanceof ModalityTerm) {
            ModalityTerm modTerm = (ModalityTerm) object;
            return modTerm.getModality().equals(getModality())
                    && modTerm.getSubterm(0).equals(getSubterm(0));
        }
        return false;
    }

    public Term getSubterm() {
        return getSubterm(0);
    }

}
