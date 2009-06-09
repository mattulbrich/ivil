/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModAssignment extends ASTModality {
    
    private Token assignedIdentifier;

    public ASTModAssignment(Token assignedIdentifier, ASTTerm assignedTerm) {
        this.assignedIdentifier = assignedIdentifier;
        addChild(assignedTerm);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getAssignedIdentifier() {
        return assignedIdentifier;
    }

    public final ASTTerm getAssignedTerm() {
        // I know this is an ASTTerm.
        return (ASTTerm)getChildren().get(0);
    }
    
    @Override
	protected Token getLocationToken() {
    	return assignedIdentifier;
	}

}
