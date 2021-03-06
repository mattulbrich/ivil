/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.environment;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.ASTLocatedElement;

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
    private final String name;

    /**
     * The operator identifier (e.g. +)
     */
    private final String opIdentifier;

    /**
     * The precedence of the operator.
     */
    private final int precedence;

    /**
     * The declaration location in the sources.
     */
    private final ASTLocatedElement declaration;

    /**
     * The arity of the operation (1 or 2).
     */
    private final int arity;

    /**
     * Instantiates a new fix operator.
     *
     * @param name
     *            the name of the underlying function
     * @param opIdentifier
     *            the symolic operator identifier
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
     * Gets the precedence of the fixed symbol.
     *
     * @return a non-negative number
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Gets the location of the declaration of this symbol.
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
     * Gets the arity of the underlying operation.
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
