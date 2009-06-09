/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.util.Util;

public class Binding extends Term {
	
	
	private Binder binder;
	private Type variableType;
	private String variableName;

	public Binding(Binder binder, Type type, Type variableType, String variableName,
			Term[] subterms) throws TermException {
		super(subterms, type);
		this.binder = binder;
		this.variableType = variableType;
		this.variableName = variableName;
		
		typeCheck();
	}
	
	public String getVariableName() {
        return variableName;
    }

    public Type getVariableType() {
        return variableType;
    }

    public Binder getBinder() {
        return binder;
    }
    
	
	private void typeCheck() throws TermException {

		if(countSubterms() != binder.getArity()) {
			throw new TermException("Binder " + binder + " expects " + 
					binder.getArity() + " arguments, but got:\n" +
					Util.listTerms(getSubterms()));
		}

		TypeUnification unify = new TypeUnification();
		Type[] argumentTypes = binder.getArgumentTypes();

		try {
			for (int i = 0; i < countSubterms(); i++) {
				unify.leftUnify(argumentTypes[i], TypeUnification.makeVariant(getSubterm(i).getType()));
			}
			unify.leftUnify(binder.getVarType(), TypeUnification.makeVariant(getVariableType()));
			unify.leftUnify(binder.getResultType(), TypeUnification.makeVariant(getType()));
		} catch(UnificationException e) {
			throw new TermException("Term " + toString() + "cannot be typed.\nFunction symbol: " + binder +
					"\nTypes of subterms:\n" + Util.listTypes(getSubterms()));
		}

	}

	@Override
	public @NonNull String toString(boolean typed) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("(").append(binder.getName()).append(" ").append(variableName);
	    if(typed) {
            sb.append(" as ").append(variableType);
        }
	    sb.append(";");
		for (int i = 0; i < countSubterms(); i++) {
			sb.append(getSubterm(i).toString(typed));
			if(i != countSubterms() - 1)
				sb.append(";");
		}
		sb.append(")");
		if(typed) {
		    sb.append(" as ").append(getType());
		}
		return sb.toString();
	}
	
	@Override public void visit(TermVisitor visitor) throws TermException {
		visitor.visit(this);
	}

    /*
     * This term is equal to another term if it is a Binding
     * and has the same binder symbol and same arguments.
     * 
     * This method is not invariant to alpha renaming.
     * 
     * TODO implement alpha-invariance?
     */
    @Override 
    public boolean equals(@NonNull Object object) {
        if (object instanceof Binding) {
            Binding bind = (Binding) object;
            if(bind.getBinder() != getBinder())
                return false;
            
            if(!bind.getType().equals(getType()))
                return false;
            
            if(!bind.getVariableName().equals(getVariableName()))
                return false;
            
            if(!bind.getVariableType().equals(getVariableType()))
                return false;
            
            assert bind.countSubterms() == countSubterms();
            
            for (int i = 0; i < countSubterms(); i++) {
                if(!bind.getSubterm(i).equals(getSubterm(i)))
                    return false;
            }
            
            return true;
        }
        return false;
    }
	


}
