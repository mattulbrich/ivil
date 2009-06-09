/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import java.util.Collection;

import de.uka.iti.pseudo.environment.Sort;

public class TypeApplication extends Type {
    
    private static final Type[] NO_ARGS = new Type[0];
    private Type[] typeParamters;
    private Sort sort;
    
    public TypeApplication(Sort sort, Type[] typeParameters) throws TermException {
    	
    	assert typeParameters != null;
    	assert sort != null;
    	
    	if(sort.getArity() != typeParameters.length)
    		throw new TermException("Sort " + sort.getName() + " expects " + sort.getArity() +
    				" parameters, but received " + typeParameters.length);
    	
        this.sort = sort;
        this.typeParamters = typeParameters;
    }
    
    public TypeApplication(Sort sort) throws TermException {
        this(sort, NO_ARGS);
    }



    @Override
    public void collectTypeVariables(Collection<String> coll) {
        for (Type tr : typeParamters) {
            tr.collectTypeVariables(coll);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sort.getName());
        for (int i = 0; i < typeParamters.length; i++) {
            sb.append(i == 0 ? "(" : ",");
            sb.append(typeParamters[i]);
        }
        if(typeParamters.length > 0)
            sb.append(")");
        
        return sb.toString();
    }
    
    @Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeApplication) {
			TypeApplication tya = (TypeApplication) obj;
			if(tya.sort != sort)
				return false;
			for (int i = 0; i < typeParamters.length; i++) {
				if(!tya.typeParamters[i].equals(typeParamters[i]))
					return false;
	        }
			return true;
		}
		return false;
	}
    
    @Override
    public Type visit(TypeVisitor visitor) throws TermException {
        return visitor.visit(this);
    }

    public Sort getSort() {
        return sort;
    }

    public Type[] getArguments() {
        return typeParamters;
    }
}
