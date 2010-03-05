/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

public class SchemaUpdateTerm extends Term {

    private String schemaIdentifier;

    public SchemaUpdateTerm(String schemaUpdateId, Term subterm) {
        super(new Term[] { subterm }, subterm.getType());
        this.schemaIdentifier = schemaUpdateId;
    }

    public boolean equals(Object object) {
        if (object instanceof SchemaUpdateTerm) {
            SchemaUpdateTerm schemaUp = (SchemaUpdateTerm) object;
            return schemaIdentifier.equals(schemaUp.getSchemaIdentifier()) &&
            getSubterm(0).equals(schemaUp.getSubterm(0));
            
        }
        return false;
    }

    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public String toString(boolean typed) {
        return "{ " + getSchemaIdentifier() + " }" + getSubterm(0).toString(typed);
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
