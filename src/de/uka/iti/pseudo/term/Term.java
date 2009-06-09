package de.uka.iti.pseudo.term;

public abstract class Term {
	
	Term[] subterms;

	protected Term(Term[] subterms) {
		this.subterms = subterms;
	}

}
