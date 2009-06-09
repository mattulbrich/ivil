/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Function;

public class Application extends Term {
	
	private Function function;

	public Application(Function funct, Term[] subterms) throws TermException {
		super(subterms);
		this.function = funct;
	}

	public Application(Function funct) throws TermException {
		super();
		this.function = funct;
	}

	protected Type inferType() {
		
//		Type[] argumentTypes = function.getArgumentTypes();
//		
//		if(argumentTypes.length != countSubterms())
//			throw new TermException("wrong number of arguments for function " + function.getName());
//		
//		// TODO
		
		return null;
	}
	
	@Override
	public String toString() {
		String retval = function.getName();
		if (countSubterms() > 0) {
			retval += "(";
			for (int i = 0; i < countSubterms(); i++) {
				retval += getSubterm(i);
				if (i != countSubterms() - 1)
					retval += ",";
			}
			retval += ")";
		}
		return retval;
	}

}
