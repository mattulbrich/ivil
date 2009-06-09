/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModWhile extends ASTModality {

    private Token headToken;
    
    public ASTModWhile(Token t, ASTTerm condTerm, ASTTerm invariant, ASTModality bodyMod) {
        this.headToken = t;
        
        addChild(condTerm);
        addChild(bodyMod);
        if(invariant != null)
            addChild(invariant);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
	protected Token getLocationToken() {
    	return headToken;
	}

    public ASTTerm getConditionTerm() {
        return (ASTTerm) getChildren().get(0);
    }

    public ASTModality getBodyModality() {
        return (ASTModality) getChildren().get(1);
    }
    
    public @Nullable ASTTerm getInvariantTerm() {
        List<ASTElement> children = getChildren();
        if(children.size() > 2)
            return (ASTTerm) children.get(2);
        else
            return null;
    }
    
}
