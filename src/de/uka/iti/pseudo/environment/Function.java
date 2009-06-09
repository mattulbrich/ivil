/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.environment;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.AssignModality.AssignTarget;

/**
 * A function is a syntactical element to which argument terms can be applied.
 * 
 * It is closely related to a {@link Binder}.
 * 
 * The involved types may contain type variables if the function is polymorphic.
 * An example of a polymorphic function is the cond function
 * <pre>
 *   'a cond(bool, 'a, 'a)
 * </pre>
 * or something like
 * <pre>
 *   'b apply(Func('a, 'b), 'b) 
 * </pre>
 * 
 * The result type may have type variables that do not appear in the arguments, for instance
 * <pre>
 *   Set('a) $EmptySet
 *   Set('a) $Nil
 * </pre>
 */
public class Function implements AssignTarget {

    /**
     * The name of the function.
     */
    private String name;

    /**
     * The result type.
     */
    private Type resultType;

    /**
     * The argument types.
     */
    private Type argumentTypes[];
    
    /**
     * A function can be tagged unique. This is a fact that can be used by rules.
     */
    private boolean unique;
    
    /**
     * A function can be tagges assignable. Only assignables can be used in assignments in modalities.
     */
    private boolean assignable;
    
    /**
     * The location of the declaration of this object.
     */
    private ASTLocatedElement declaration;

    /**
     * Instantiates a new function symbol object
     * 
     * @param name
     *            an identifier (possibly beginning with $)
     * @param resultType
     *            the result type of the function
     * @param argumentTypes
     *            the argument types
     * @param unique
     *            true if this is unique
     * @param assignable
     *            true if this is assignable
     * @param declaration
     *            the declaration of this function symbol
     */
    public Function(@NonNull String name, @NonNull Type resultType,
            @NonNull Type[] argumentTypes, boolean unique, boolean assignable, 
            @NonNull ASTLocatedElement declaration) {
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
        this.unique = unique;
        this.assignable = assignable;
        
        assert !assignable || getArity() == 0;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the result type. The type may contain type variables.
     * 
     * @return the result type
     */
    public Type getResultType() {
        return resultType;
    }

    /**
     * Gets the argument types.
     * The arity of this function is the length of this array.
     * The types may contain type variables.
     * 
     * @return the argument types
     */
    public Type[] getArgumentTypes() {
        return argumentTypes;
    }

    /**
     * Gets the declaration.
     * 
     * @return the declaration
     */
    public ASTLocatedElement getDeclaration() {
        return declaration;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Function[" + resultType + " " + name);
    	if(getArity() > 0) {
    		for (int i = 0; i < argumentTypes.length; i++) {
    			sb.append(i == 0 ? "(" : ", ");
				sb.append(argumentTypes[i]);
			}
    		sb.append(")");
    	}
    	sb.append("]");
        return sb.toString();
    }

	/**
     * Checks if this is a unique function.
     * 
     * @return true, if this is unique
     */
	public boolean isUnique() {
		return unique;
	}
	
    /**
     * Checks if is assignable.
     * 
     * @return true, if this is assignable
     */
    public boolean isAssignable() {
        return assignable;
    }

	/**
     * Gets the arity, i.e. the number of expected arguments for this function symbol
     * 
     * @return the arity of this function
     */
	public int getArity() {
		return argumentTypes.length;
	}

}
