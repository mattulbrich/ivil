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

public class ASTModIf extends ASTModality {
    
    private Token headToken;
    private ASTTerm conditionTerm;
    private ASTModality thenModality;
    private ASTModality elseModality;

    public ASTModIf(Token t, ASTTerm condTerm, ASTModality thenMod, ASTModality elseMod) {
        this.headToken = t;
        this.conditionTerm = condTerm;
        this.thenModality = thenMod;
        this.elseModality = elseMod;
        
        addChild(condTerm);
        addChild(thenMod);
        addChild(elseMod);
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
