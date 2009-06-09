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
	}

	@Override
	protected void visit(TermVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "[MOD]" + getSubterm(0);
	}

}
