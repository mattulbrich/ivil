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
import java.util.List;

import nonnull.NonNull;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTAssignment;

public class ASTUpdateTerm extends ASTTerm {

    public ASTUpdateTerm(@NonNull List<ASTAssignment> assignments, @NonNull ASTTerm term) {
        super(Collections.singletonList(term));
        addChildren(assignments);
    }

    @Override public Token getLocationToken() {
        Token locationToken = getChildren().get(0).getLocationToken();
        
        assert locationToken != null : "nullness: this first child is an assignment, hence, has a first token";
        
        return locationToken;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
