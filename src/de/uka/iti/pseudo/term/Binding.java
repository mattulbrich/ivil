package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Binder;

public class Binding extends Term {

	public Binding(Binder binder, Type variableType, String variableName,
			Term[] subterms) throws TermException {
		super(subterms);
	}

	@Override
	protected Type inferType() {
		// TODO Auto-generated method stub
		return null;
	}

}
