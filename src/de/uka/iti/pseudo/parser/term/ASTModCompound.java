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

public class ASTModCompound extends ASTModality {
    
    public ASTModCompound(ASTModality modality1, ASTModality modality2) {
        addChild(modality1);
        addChild(modality2);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return getModality1().getLocationToken();
	}

    public ASTModality getModality1() {
        return (ASTModality) getChildren().get(0);
    }

    public ASTModality getModality2() {
        return (ASTModality) getChildren().get(1);
    }

}
