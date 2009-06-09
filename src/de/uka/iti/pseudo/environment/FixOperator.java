/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;

public class FixOperator {

    private String name;

    private String opIdentifier;

    private int precedence;

    private ASTFunctionDeclaration declaration;

    private int arity;

    public FixOperator(String name, String opIdentifier, int precedence,
            int arity, ASTFunctionDeclaration declaration) {
        super();
        this.name = name;
        this.opIdentifier = opIdentifier;
        this.precedence = precedence;
        this.arity = arity;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public String getOpIdentifier() {
        return opIdentifier;
    }

    public int getPrecedence() {
        return precedence;
    }

    public ASTFunctionDeclaration getDeclaration() {
        return declaration;
    }
    
    @Override
    public String toString() {
        return "FixOperator[op: " +opIdentifier + "; function: " + name + "; precedence: " +precedence +"]"; 
    }

    public boolean isUnary() {
        return arity == 1;
    }

    public int getArity() {
        return arity;
    }

}
