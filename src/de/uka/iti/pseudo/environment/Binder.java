/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.TypingContext;

// TODO: Auto-generated Javadoc
/**
 * A binder is a syntactical element that binds a single variable.
 * 
 * It has one or more subterms (apart from the variable which is not a subterm).
 * The name of a binder always starts with a backslash "\".
 * It is closely realted to {@link Function}.
 * 
 * @author mattias ulbrich
 * 
 */
public class Binder {
    
    /**
	 * The name of the binder, starting with a backslash, non-null
	 */
    private String name;

    /**
	 * The result type 
	 */
    private Type resultType;

    /**
	 * The argument types.
	 */
    private Type argumentTypes[];

    /**
	 * The declaration.
	 */
    private ASTBinderDeclaration declaration;

    /**
	 * The var type.
	 */
    private Type varType;

    /**
	 * Instantiates a new binder.
	 * 
	 * @param name
	 *            the name
	 * @param resultType
	 *            the result type
	 * @param varTy
	 *            the var ty
	 * @param argumentTypes
	 *            the argument types
	 * @param declaration
	 *            the declaration
	 */
    public Binder(String name, Type resultType, Type varTy,
            Type[] argumentTypes, ASTBinderDeclaration declaration) {
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
        this.varType = varTy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String ret = "Binder[" + name + ";ret: " + resultType + 
            ";var: " + varType + ":args:";
        for (Type tr : argumentTypes) {
            ret += " " + tr;
        }
        return ret + "]";
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
	 * Gets the result type.
	 * 
	 * @return the result type
	 */
    public Type getResultType() {
        return resultType;
    }

    /**
	 * Gets the argument types.
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
    public ASTBinderDeclaration getDeclaration() {
        return declaration;
    }

    /**
	 * Gets the var type.
	 * 
	 * @return the var type
	 */
    public Type getVarType() {
        return varType;
    }

	public int getArity() {
		return getArgumentTypes().length;
	}

}
