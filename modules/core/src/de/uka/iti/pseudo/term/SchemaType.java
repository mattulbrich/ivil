/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.term.creation.TypeUnification;
import nonnull.NonNull;
import nonnull.Nullable;

/**
 * This class encapsulates a named instantiatable type placeholder.
 * 
 * <p>
 * All schema types are printed prefixed with a percent and a prime
 * symbol (<code>%'</code>). <b>Their name, however, does not include that prefix.</b>
 * 
 * <p>
 * Like schema variables and schema updates, these types are not meant to appear
 * at toplevel but should be instantiated at that time.
 * 
 * @see TypeApplication
 * @see SchemaType
 */

public class SchemaType extends Type {
    
    /**
     * The prefix used to distinguish a type variable from its variant.
     * @see TypeUnification
     */
    public static final String VARIANT_PREFIX = "#";

    /**
     * The name (w/o leading %')
     */
    private String name;

    /**
     * Instantiates a new schema type variable.
     * 
     * @param typeVar
     *            the name of the schema type (without leading %')
     */
    public SchemaType(@NonNull String typeVar) {
        this.name = typeVar;
    }

    /**
     * A type variable is rendered to a string by prepending a prime ' to its
     * name.
     */
    @Override
    public String toString() {
        return "%'" + getVariableName();
    }

    /**
     * Gets the variable name w/o the leading prefix %'.
     * 
     * @return the variable name
     */
    public @NonNull String getVariableName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.iti.pseudo.term.Type#visit(de.uka.iti.pseudo.term.TypeVisitor)
     */
    @Override
    public <R,A> R accept(TypeVisitor<R,A> visitor, A parameter) throws TermException {
        return visitor.visit(this, parameter);
    }

    /**
     * Two type variables are equal iff their names are equal.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SchemaType) {
            SchemaType tyv = (SchemaType) obj;
            return name.equals(tyv.name);
        }
        return false;
    }

    /**
     * the hashcode of a type variable is the hash code of its name.S
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
