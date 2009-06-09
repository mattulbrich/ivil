package de.uka.iti.pseudo.term;

public class ModalityTerm extends Term {

	private Modality modality; 

	public ModalityTerm(Modality modality, Term subterm) {
		super(new Term[] { subterm }, subterm.getType());
	}

}
