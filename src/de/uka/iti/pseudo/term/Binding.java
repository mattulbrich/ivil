package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Binder;

public class Binding extends Term {
	
	private Binder binder;
	private Type variableType;
	private String variableName;

	public Binding(Binder binder, Type variableType, String variableName,
			Term[] subterms) throws TermException {
		super(subterms);
		this.binder = binder;
		this.variableType = variableType;
		this.variableName = variableName;
	}

	@Override
	protected Type inferType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		String retval = "(" + binder.getName() + ";" + variableType + " " + variableName + ";";
		for (int i = 0; i < countSubterms(); i++) {
			retval += getSubterm(i);
			if(i != countSubterms() - 1)
				retval += ";";
		}
		retval += ")";
		return retval;
	}

}
