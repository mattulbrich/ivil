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

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTSchemaUpdateTerm extends ASTTerm {

    private Token identifierToken;

    public ASTSchemaUpdateTerm(Token id, ASTTerm term) {
        super(Collections.singletonList(term));
        this.identifierToken = id;
    }

    @Override public Token getLocationToken() {
        return identifierToken;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getIdentifierToken() {
        return identifierToken;
    }

}
