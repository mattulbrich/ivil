/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

public class ModalityTerm extends Term {

	private Modality modality; 

	public ModalityTerm(Modality modality, Term subterm) {
		super(new Term[] { subterm }, subterm.getType());
		this.modality = modality;
	}

	@Override public void visit(TermVisitor visitor) throws TermException {
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

}
