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
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTLocatedTerm extends ASTElement {
    
    private MatchingLocation matchingLocation;

    public ASTLocatedTerm(ASTTerm rt, MatchingLocation matchingLocation) {
        this.matchingLocation = matchingLocation;
        addChild(rt);
    }

    @Override 
    public Token getLocationToken() {
        return getTerm().getLocationToken();
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public MatchingLocation getMatchingLocation() {
        return matchingLocation;
    }
    
    public ASTTerm getTerm() {
        return (ASTTerm) getChildren().get(0);
    }

}
