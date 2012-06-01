/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * This class encapsulates a type variable type with an arbitrary name.
 *
 * <p>
 * All type variables are printed prefixed with a prime symbol. <b>Their name,
 * however, does not include that prime.</b>
 *
 * <p>
 * Type variables stand for one ordinary type (variable free expression over the
 * type constructors) in one interpretation. They are not meant to be
 * instantiated. SchemaTypeVariables may be instantiated.
 *
 * @see TypeApplication
 * @see SchemaType
 */
public final class TypeVariable extends Type {

    /**
     * a predefined type variable for convenience.
     */
    public final static TypeVariable ALPHA = TypeVariable.getInst("a");

    /**
     * a second predefined type variable for convenience.
     */
    public final static TypeVariable BETA = TypeVariable.getInst("b");

    /**
     * The actual name (w/o leading ').
     */
    private final String name;

    /**
     * Instantiates a new type variable.
     *
     * @param typeVar
     *            the name of the type variables (without leading ')
     */
    private TypeVariable(@NonNull String typeVar) {
        this.name = typeVar;
    }

    /**
     * Gets a type variable instance for a name.
     *
     * If a type with the given arguments already exists in the system, a
     * reference to the existing object is returned instead of a freshly created
     * one. If not, a new instance is created.
     *
     * @param typeVar
     *            the name of the type variables (without leading ')
     * @return a type variable with the given name. Not necessarily freshly
     *         created.
     */
    public static TypeVariable getInst(@NonNull String typeVar) {
        return (TypeVariable) new TypeVariable(typeVar).intern();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * A type variable is rendered to a string by prepending a prime ' to its
     * name.
     */
    @Override
    public String toString() {
        return "'" + getVariableName();
    }

    /**
     * Gets the variable name w/o the leading prime '.
     *
     * @return the variable name
     */
    public @NonNull String getVariableName() {
        return name;
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Type#visit(de.uka.iti.pseudo.term.TypeVisitor)
     */
    @Override @SuppressWarnings("nullness")
    public </*@Nullable*/ R, /*@Nullable*/ A>
    R accept(TypeVisitor<R,A> visitor, A parameter) throws TermException {
        return visitor.visit(this, parameter);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Two type variables are equal iff their names are equal.
     */
    // Checkstyle: IGNORE EqualsHashCode - done in Type.java
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TypeVariable) {
            TypeVariable tyv = (TypeVariable) obj;
            return name.equals(tyv.name);
        }
        return false;
    }

}
