package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Function;

public class Application extends Term {
	
	Function function;

	public Application(Function funct, Term[] subterms) throws TermException {
		super(subterms);
		
		inferType();
	}

	private void inferType() {
		// TODO
		
		Type[] argumentTypes = function.getArgumentTypes();
		
	}

}
