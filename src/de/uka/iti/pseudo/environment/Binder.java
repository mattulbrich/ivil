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

public class Binder {
    
    private String name;

    private Type resultType;

    private Type argumentTypes[];

    private ASTBinderDeclaration declaration;

    private Type varType;

    public Binder(String name, Type resultType, Type varTy,
            Type[] argumentTypes, ASTBinderDeclaration declaration) {
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
        this.varType = varTy;
    }

    public String toString() {
        String ret = "Binder[" + name + ";ret: " + resultType + 
            ";var: " + varType + ":args:";
        for (Type tr : argumentTypes) {
            ret += " " + tr;
        }
        return ret + "]";
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

    public ASTBinderDeclaration getDeclaration() {
        return declaration;
    }

    public Type getVarType() {
        return varType;
    }
    
    

}
