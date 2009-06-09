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

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModIf extends ASTModality {
    
    private Token headToken;
    private boolean hasElse;

    public ASTModIf(@NonNull Token t, 
            @NonNull ASTTerm condTerm, 
            @NonNull ASTModality thenMod, 
            @Nullable ASTModality elseMod) {
        this.headToken = t;
        
        addChild(condTerm);
        addChild(thenMod);
        if(elseMod != null) {
            hasElse = true;
            addChild(elseMod);
        } else {
            hasElse = false;
        }
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

    public ASTModality getThenModality() {
        return (ASTModality) getChildren().get(1);
    }

    public @NonNull ASTModality getElseModality() {
        assert hasElseModality();
        return (ASTModality) getChildren().get(2);
    }
    
    public boolean hasElseModality() {
        return hasElse;
    }

}
