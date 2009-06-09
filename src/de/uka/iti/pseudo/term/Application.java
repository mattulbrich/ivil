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
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.util.Util;

public class Application extends Term {
	
	private Function function;

	public Application(Function funct, Type type, Term[] subterms) throws TermException {
		super(subterms, type);
		this.function = funct;
		typeCheck();
	}

	public Application(Function funct, Type type) throws TermException {
		super(type);
		this.function = funct;
		typeCheck();
	}
	
	private void typeCheck() throws TermException {
		
		if(countSubterms() != function.getArity()) {
			throw new TermException("Function " + function + " expects " + 
					function.getArity() + " arguments, but got:\n" +
					Util.listTerms(getSubterms()));
		}
		
		TypeUnification unify = new TypeUnification();
		Type[] argumentTypes = function.getArgumentTypes();
		
		try {
			for (int i = 0; i < countSubterms(); i++) {
				unify.leftUnify(argumentTypes[i], TypeUnification.makeVariant(getSubterm(i).getType()));
			}
			unify.leftUnify(function.getResultType(), TypeUnification.makeVariant(getType()));
		} catch(UnificationException e) {
			throw new TermException("Term " + toString() + "cannot be typed.\nFunction symbol: " + function +
					"\nTypes of subterms:\n" + Util.listTypes(getSubterms()));
		}
		
	}

	@Override
	public String toString(boolean typed) {
		String retval = function.getName();
		if (countSubterms() > 0) {
			retval += "(";
			for (int i = 0; i < countSubterms(); i++) {
				retval += getSubterm(i).toString(typed);
				if (i != countSubterms() - 1)
					retval += ",";
			}
			retval += ")";
		}
		if(typed)
		    retval += " as " + getType();
		return retval;
	}
	
	@Override
	protected void visit(TermVisitor visitor) {
		visitor.visit(this);
	}


}
