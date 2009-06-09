/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.TypingContext;

public class Function {

    private String name;

    private Type resultType;

    private Type argumentTypes[];
    
    private boolean unique;
    
    private ASTLocatedElement declaration;

    public Function(String name, Type resultType,
            Type[] argumentTypes, boolean unique, ASTLocatedElement declaration) {
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public Type getResultType() {
        return resultType;
    }

    public Type[] getArgumentTypes() {
        return argumentTypes;
    }

    public ASTLocatedElement getDeclaration() {
        return declaration;
    }
    
    public String toString() {
        String ret = "Function[" + name + ";ret: " + resultType +";args:";
        for (Type tr : argumentTypes) {
            ret += " " + tr;
        }
        return ret + "]";
    }

	public boolean isUnique() {
		return unique;
	}
	

}
