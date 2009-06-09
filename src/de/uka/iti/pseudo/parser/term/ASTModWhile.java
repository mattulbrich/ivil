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

public class ASTModWhile extends ASTModality {

    private Token headToken;
    private ASTTerm conditionTerm;
    private ASTModality bodyModality;
    
    public ASTModWhile(Token t, ASTTerm condTerm, ASTModality bodyMod) {
        this.headToken = t;
        this.conditionTerm = condTerm;
        this.bodyModality = bodyMod;
        
        addChild(condTerm);
        addChild(bodyMod);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
	protected Token getLocationToken() {
    	return headToken;
	}
    
}
