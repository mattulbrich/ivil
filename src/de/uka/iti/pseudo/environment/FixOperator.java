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

// TODO: Auto-generated Javadoc

/**
 * The Class FixOperator provides a connection between function symbols and
 * inifix (or prefix) written operator symbols.
 * 
 * The arity is stored redundantly to the {@link Function}
 */

public class FixOperator {

    /**
     * The name of the operator as a function symbol (e.g. $add)
     */
    private String name;

    /**
     * The operator identifier (e.g. +)
     */
    private String opIdentifier;

    /**
     * The precedence of the operator
     */
    private int precedence;

    /**
     * The declaration location in the sources.
     */
    private ASTLocatedElement declaration;

    /**
     * The arity of the operation
     */
    private int arity;

    /**
     * Instantiates a new fix operator.
     * 
     * @param name
     *            the name
     * @param opIdentifier
     *            the operator identifier
     * @param precedence
     *            the precedence (>= 0)
     * @param arity
     *            the arity of the symbol (1 or 2)
     * @param declaration
     *            the declaration location
     */
    public FixOperator(@NonNull String name, @NonNull String opIdentifier, 
            @NonNull int precedence,
            @NonNull int arity, @NonNull ASTLocatedElement declaration) {
        super();
        this.name = name;
        this.opIdentifier = opIdentifier;
        this.precedence = precedence;
        this.arity = arity;
        this.declaration = declaration;
        
        assert arity == 1 || arity == 2;
        assert precedence >= 0;
    }

    /**
     * Gets the function symbol name for the operator.
     * Often they are marked as internal names with a leading $.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the operator identifier.
     * consisting of one or more special characters.
     * 
     * @return the operator characters
     */
    public String getOpIdentifier() {
        return opIdentifier;
    }

    /**
     * Gets the precedence.
     * 
     * @return the precedence
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Gets the declaration.
     * 
     * @return the declaration
     */
    public ASTLocatedElement getDeclaration() {
        return declaration;
    }

    /**
     * Checks if the fix operator is unary.
     * 
     * @return true, iff the arity == 1
     */
    public boolean isUnary() {
        return arity == 1;
    }

    /**
     * Gets the arity of the underlying operation
     * 
     * @return the arity
     */
    public int getArity() {
        return arity;
    }
    
    @Override 
    public String toString() {
        return "FixOperator[op: " + opIdentifier + "; function: " + name
                + "; precedence: " + precedence + "]";
    }

}
