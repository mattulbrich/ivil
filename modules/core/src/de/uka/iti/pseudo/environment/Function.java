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

import java.util.Arrays;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Type;

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
public class Function implements Named {

    /**
     * The name of the function.
     */
    private final String name;

    /**
     * The result type.
     */
    private final Type resultType;

    /**
     * The argument types.
     */
    private final Type[] argumentTypes;

    /**
     * A function can be tagged unique. This is a fact that can be used by rules.
     */
    private final boolean unique;

    /**
     * A function can be tagged assignable. Only assignables can be used in
     * assignments in modalities.
     */
    private final boolean assignable;

    /**
     * The location of the declaration of this object.
     */
    private final ASTLocatedElement declaration;

    /**
     * Instantiates a new function symbol object.
     * Only function symbol without parameters can be assignable.
     * A function symbol cannot be both unique and assignable.
     *
     * @param name
     *            an identifier (possibly beginning with $)
     * @param resultType
     *            the result type of the function, must not contain schema types
     * @param argumentTypes
     *            the argument types, must not contain schema types
     * @param unique
     *            true if this is unique
     * @param assignable
     *            true if this is assignable
     * @param declaration
     *            the declaration of this function symbol
     * @throws EnvironmentException
     *            if the arguments do not describe a valid function declaration
     */
    public Function(@NonNull String name, @NonNull Type resultType,
            @NonNull Type[] argumentTypes, boolean unique, boolean assignable,
            @NonNull ASTLocatedElement declaration) throws EnvironmentException {
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
        this.unique = unique;
        this.assignable = assignable;

        if(assignable && getArity() != 0) {
            throw new EnvironmentException("Assignables must have arity 0: " + name);
        }

        if(unique && assignable) {
            throw new EnvironmentException("Assignables must not be unique: " + name);
        }

        if (!TypeVariableCollector.collectSchema(resultType).isEmpty()) {
            throw new EnvironmentException(
                    "Result types must not contain schema types: " + resultType
                            + " for " + name);
        }

        if(!TypeVariableCollector.collectSchema(Arrays.asList(argumentTypes)).isEmpty()) {
            throw new EnvironmentException(
                    "Argument types must not contain schema types: "
                            + Arrays.asList(argumentTypes)
                            + " for " + name);
        }
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
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
    @Override
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

    /**
     * {@inheritDoc}
     *
     * <p>We use the hash code of the name as our hash code.
     * This guarantees identical hash codes of the runs.
     */
    @Override public int hashCode() {
        return name.hashCode();
    }

}
