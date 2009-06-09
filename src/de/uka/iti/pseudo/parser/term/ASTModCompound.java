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
    
    private ASTModality modality1;
    private ASTModality modality2;

    public ASTModCompound(ASTModality mod1, ASTModality mod2) {
        this.modality1 = mod1;
        this.modality2 = mod2;
        
        addChild(modality1);
        addChild(modality2);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return modality1.getLocationToken();
	}

}
