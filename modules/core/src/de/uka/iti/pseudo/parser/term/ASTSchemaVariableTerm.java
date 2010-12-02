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
package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import nonnull.NonNull;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTSchemaVariableTerm extends ASTTerm {
    
    private Token schemaToken;

    public ASTSchemaVariableTerm(Token t) {
        super(Collections.<ASTTerm>emptyList());
        schemaToken = t;
    }

    @Override public Token getLocationToken() {
        return schemaToken;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    /**
     * return the name of this schema variable with the leading "%".
     * @return a string of positive length 
     */
    public @NonNull String getName() {
        return schemaToken.image;
    }

    public Token getToken() {
        return schemaToken;
    }

}
