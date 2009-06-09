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

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.term.creation.Typing;

public class ASTBinderTerm extends ASTTerm {
    
    private Typing variableTyping;

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
    
    @Override
	protected Token getLocationToken() {
    	return binderToken;
	}

    public Typing getVariableTyping() {
        return variableTyping;
    }

    public void setVariableTyping(Typing variableTyping) {
        this.variableTyping = variableTyping;
    }

}
