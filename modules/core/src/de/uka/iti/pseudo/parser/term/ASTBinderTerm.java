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

import java.util.List;

import nonnull.Nullable;
import checkers.nullness.quals.LazyNonNull;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.term.creation.Typing;

public class ASTBinderTerm extends ASTTerm {
    
    private @LazyNonNull Typing variableTyping = null;

    private Token binderToken;
    private ASTType variableType;
    private Token variableToken;
    
    public ASTBinderTerm(Token binderToken, ASTType variableType,
            Token variableToken, List<ASTTerm> subterms) {
        super(subterms);
        this.binderToken = binderToken;
        this.variableType = variableType;
        this.variableToken = variableToken;
        
        if(variableType != null)
            addChild(variableType);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getBinderToken() {
        return binderToken;
    }

    public final ASTType getVariableType() {
        return variableType;
    }

    public final Token getVariableToken() {
        return variableToken;
    }
    
	public Token getLocationToken() {
    	return binderToken;
	}

    public @Nullable Typing getVariableTyping() {
        return variableTyping;
    }

    public void setVariableTyping(Typing variableTyping) {
        this.variableTyping = variableTyping;
    }

}
