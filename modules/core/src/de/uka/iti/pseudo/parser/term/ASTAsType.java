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

public class ASTAsType extends ASTTerm {

    private ASTType asType;

    public ASTAsType(ASTTerm term, ASTType type) {
        super(Collections.<ASTTerm>singletonList(term));
        addChild(type);
        this.asType = type;
    }

    @Override
    public Token getLocationToken() {
        return getSubterms().get(0).getLocationToken();
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTType getAsType() {
        return asType;
    }
    
    public ASTTerm getTerm() {
        return getSubterms().get(0);
    }

}
