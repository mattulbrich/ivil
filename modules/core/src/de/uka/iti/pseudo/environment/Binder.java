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
import de.uka.iti.pseudo.term.Type;

/**
 * A binder is a syntactical element that binds a single variable.
 *
 * It has one or more subterms (apart from the variable which is not a subterm).
 * The name of a binder always starts with a backslash "\". It is closely
 * realted to a {@link Function}.
 *
 * The involved types may contain type variables if the binder is polymorphic.
 * An example of a polymorphic binder is the choose binder
 * <pre>
 *   'a (\choose 'a; bool)
 * </pre>
 * or something like
 * <pre>
 *   Func('a,'b) (\lambda 'a; 'b)
 * </pre>
 *
 */
public class Binder {

    /**
     * The name of the binder, starting with a backslash.
     */
    private final String name;

    /**
     * The result type of this binder.
     */
    private final Type resultType;

    /**
     * The type of the bound variable.
     */
    private final Type varType;

    /**
     * The types of the arguments to this binder.
     */
    private final Type[] argumentTypes;

    /**
     * The declaration in the enviroment file.
     */
    private final ASTLocatedElement declaration;

    /**
     * Instantiates a new binder.
     *
     * @param name
     *            the name of the binder, must begin with a backslash
     * @param resultType
     *            the result type of this binder object
     * @param varTy
     *            the type of the variable
     * @param argumentTypes
     *            the argument types
     * @param declaration
     *            the declaration location in the sources
     */
    public Binder(@NonNull String name, @NonNull Type resultType,
            @NonNull Type varTy, @NonNull Type[] argumentTypes,
            @NonNull ASTLocatedElement declaration) {
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
        this.varType = varTy;
    }

    /**
     * Get the name of this binder.
     *
     * @return the name of the binder, a string beginning with a backslash
     */
    public @NonNull String getName() {
        return name;
    }

    /**
     * the result type of this binder. This may contain type variables
     *
     * @return the type of the resulting term
     */
    public @NonNull Type getResultType() {
        return resultType;
    }

    /**
     * the type that the bound variable has. may contain type variables
     *
     * @return a type, possible with type variables
     */
    public @NonNull Type getVarType() {
        return varType;
    }

    /**
     * the types of the arguments. the length of this array is the arity of the
     * binder. The types in this array may contain Type variables.
     *
     * @return the expected arguments as array
     */
    public @NonNull Type[] getArgumentTypes() {
        return argumentTypes;
    }

    /**
     * Get the declaration location.
     *
     * @return the located element describing the definition location for this
     *         binder object.
     */
    public @NonNull ASTLocatedElement getDeclaration() {
        return declaration;
    }

    /**
     * Get the arity of this binder (excludes the variable).
     *
     * @return the number of arguments the binder expects (excluding the
     *         variable position)
     */
    public int getArity() {
        return getArgumentTypes().length;
    }

    @Override
    public String toString() {
        String ret = "Binder[" + name + ";ret: " + resultType + ";var: "
                + varType + ":args:";
        for (Type tr : argumentTypes) {
            ret += " " + tr;
        }
        return ret + "]";
    }

}
