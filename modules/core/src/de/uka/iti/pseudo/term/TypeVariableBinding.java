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
import de.uka.iti.pseudo.environment.Environment;

/**
 * The Class TypeVariableBinding encapsulates type variable bindings as terms.
 *
 * There are currently two {@link Kind}s of bindings: Universal and existential.
 * A type variable binding takes a type variable and a boolean term as arguments.
 * The type variable is bound in its context.
 *
 * A schema type can also be bound (used in terms appearing in rules).
 */
public final class TypeVariableBinding extends Term {

    /**
     * The enumeration of all kinds of type variable bindings.
     */
    public static enum Kind {

        /**
         * Universal type quantification.
         */
        ALL("\\T_all"),

        /**
         * Existential type quantification.
         */
        EX("\\T_ex");

        /**
         * The enum constants carry their logical representation with them.
         */
        private final String image;

        private Kind(String image) {
            this.image = image;
        }

        @Override
        public String toString() {
            return image;
        }
    };

    /**
     * The kind (ALL or EX) of this binding.
     */
    private final Kind kind;

    /**
     * The bound type variable, or schema type.
     */
    private final Type boundType;

    /**
     * Instantiates a new type variable binding.
     *
     * @param kind
     *            the kind
     * @param boundType
     *            the bound type
     * @param subterm
     *            the term in which the type is bound
     * @throws TermException
     *             if the boundType is not a type variable or schema type, or
     *             the argument is not boolean
     */
    private TypeVariableBinding(@NonNull Kind kind, @NonNull Type boundType,
            @NonNull Term subterm) throws TermException {
        super(new Term[] { subterm }, Environment.getBoolType());

        this.kind = kind;
        this.boundType = boundType;

        typeCheck(boundType, subterm);

    }

    /**
     * Gets a type variable binding term.
     *
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     *
     * @param kind
     *            the kind of quantification
     * @param boundType
     *            the bound type
     * @param subterm
     *            the term in which the type is bound
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     * @throws TermException
     *             if the boundType is not a type variable or schema type, or
     *             the argument is not boolean
     */
    public static TypeVariableBinding getInst(@NonNull Kind kind, @NonNull Type boundType,
            @NonNull Term subterm) throws TermException {
        return (TypeVariableBinding) new TypeVariableBinding(kind, boundType, subterm).intern();
    }

    /*
     * Type check the arguments: (1) boundtype must be type variable or schema
     * type, (2) subterm must be boolean
     */
    private static void typeCheck(Type boundType, Term subterm) throws TermException {
        if (!subterm.getType().equals(Environment.getBoolType())) {
            throw new TermException(
                    "TypeVariableBinding takes a boolean argument, not of type "
                            + subterm.getType());
        }

        if (!(boundType instanceof TypeVariable || boundType instanceof SchemaType)) {
            throw new TermException(
                    "TypeVariableBinding binds a type variable or a schema type, not "
                            + boundType);
        }
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#equals(java.lang.Object)
     * Checkstyle: IGNORE EqualsHashCode - in Term.java
     */
    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof TypeVariableBinding){
            TypeVariableBinding tvb = (TypeVariableBinding) object;
            if(tvb.getKind() != getKind()) {
                return false;
            }

            if(!tvb.getBoundType().equals(getBoundType())) {
                return false;
            }

            if(!tvb.getSubterm(0).equals(getSubterm(0))) {
                return false;
            }

            return true;
        }
        return false;
    }

    /*
     * This implementation incorporates the type variable symbol into the calculation.
     */
    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#calculateHashCode()
     */
    @Override
    protected int calculateHashCode() {
        return super.calculateHashCode() * 31 + boundType.hashCode();
    }


    /**
     * Get the {@link Kind} of this type variable binding.
     *
     * @return a kind indicating whether universal or existential quantification
     */
    public @NonNull Kind getKind() {
        return kind;
    }

    /**
     * Gets the type bound in this binding.
     *
     * @return a type variable or a schema type
     */
    public @NonNull Type getBoundType() {
        return boundType;
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#toString(boolean)
     */
    @Override
    public String toString(boolean typed) {

        return "(" + kind.image + " " + boundType + ";"
                + getSubterm(0).toString(typed) + (typed ? ") as bool" : ")");
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#visit(de.uka.iti.pseudo.term.TermVisitor)
     */
    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Get the subterm. Type variable bindings only have one subterm. Return it.
     *
     * @return the only subterm of the type variable binding
     */
    public @NonNull Term getSubterm() {
        return getSubterm(0);
    }

}
