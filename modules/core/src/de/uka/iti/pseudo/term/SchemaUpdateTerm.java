/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;

// TODO DOC

@NonNull
public final class SchemaUpdateTerm extends Term {

    private final String schemaIdentifier;
    private final boolean optional;

    private SchemaUpdateTerm(String schemaUpdateId, boolean optional, Term subterm) {
        super(new Term[] { subterm }, subterm.getType());
        this.schemaIdentifier = schemaUpdateId;
        this.optional = optional;
    }
    
    public static SchemaUpdateTerm getInst(String schemaUpdateId, boolean optional, Term subterm) {
        return (SchemaUpdateTerm) new SchemaUpdateTerm(schemaUpdateId, optional, subterm).intern();
    }

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
    @Override
    protected int calculateHashCode() {
        return schemaIdentifier.hashCode();
    }


    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public String toString(boolean typed) {
        return "{ " + getSchemaIdentifier() + (isOptional() ? " ?}" : " }")
                + getSubterm(0).toString(typed);
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public boolean isOptional() {
        return optional;
    }

}
