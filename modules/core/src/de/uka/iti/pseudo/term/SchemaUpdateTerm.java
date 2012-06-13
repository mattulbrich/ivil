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
 * The Class SchemaUpdateTerm is used for update terms in which the update is
 * schematic.
 *
 * The schema update is denoted by a schema identifier. Additionally, the update
 * may be marked as optional.
 */

@NonNull
public final class SchemaUpdateTerm extends Term {

    /**
     * The schema identifier.
     */
    private final String schemaIdentifier;

    /**
     * A flag whether the update is optional or not.
     */
    private final boolean optional;

    /**
     * Instantiates a new schema update term.
     *
     * @param schemaUpdateId
     *            the schema update name
     * @param optional
     *            is the update optional or mandatory
     * @param subterm
     *            the updated term
     */
    private SchemaUpdateTerm(String schemaUpdateId, boolean optional, Term subterm) {
        super(new Term[] { subterm }, subterm.getType());
        this.schemaIdentifier = schemaUpdateId;
        this.optional = optional;
    }

    /**
     * Instantiates a new schema update term.
     *
     * @param schemaUpdateId
     *            the schema update name
     * @param optional
     *            is the update optional or mandatory
     * @param subterm
     *            the updated term
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     */
    public static SchemaUpdateTerm getInst(String schemaUpdateId, boolean optional, Term subterm) {
        return (SchemaUpdateTerm) new SchemaUpdateTerm(schemaUpdateId, optional, subterm).intern();
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#equals(java.lang.Object)
     * Checkstyle: IGNORE EqualsHashCode
     */
    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof SchemaUpdateTerm) {
            SchemaUpdateTerm schemaUp = (SchemaUpdateTerm) object;
            return schemaIdentifier.equals(schemaUp.getSchemaIdentifier()) &&
            getSubterm(0).equals(schemaUp.getSubterm(0));

        }
        return false;
    }

    /*
     * This implementation takes the hash code from the identifier
     */
    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#calculateHashCode()
     */
    @Override
    protected int calculateHashCode() {
        return schemaIdentifier.hashCode();
    }


    /**
     * Gets the schema identifier.
     *
     * @return the schema identifier
     */
    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#toString(boolean)
     */
    @Override
    public String toString(boolean typed) {
        return "{ " + getSchemaIdentifier() + (isOptional() ? " ?}" : " }")
                + getSubterm(0).toString(typed);
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.term.Term#visit(de.uka.iti.pseudo.term.TermVisitor)
     */
    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Checks if is optional.
     *
     * @return true, if is optional
     */
    public boolean isOptional() {
        return optional;
    }

}
