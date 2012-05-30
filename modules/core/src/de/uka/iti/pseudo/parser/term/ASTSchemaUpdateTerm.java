/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTSchemaUpdateTerm extends ASTTerm {

    private final Token identifierToken;
    private final boolean optional;

    public ASTSchemaUpdateTerm(Token id, boolean optional, ASTTerm term) {
        super(Collections.singletonList(term));
        this.identifierToken = id;
        this.optional = optional;
    }

    @Override 
    public Token getLocationToken() {
        return identifierToken;
    }

    @Override 
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getIdentifierToken() {
        return identifierToken;
    }
    
    public boolean isOptional() {
        return optional;
    }

}
