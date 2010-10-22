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
import de.uka.iti.pseudo.util.Pair;

public class ASTBinderTerm extends ASTTerm {
    
    private @LazyNonNull Typing variableTyping = null;

    private Token binderToken;

    private List<Pair<Token, ASTType>> boundVars;
    
    public ASTBinderTerm(Token binderToken, List<Pair<Token, ASTType>> boundVars,
            List<ASTTerm> subterms) {
        super(subterms);
        this.binderToken = binderToken;
        this.boundVars = boundVars;
        
        for (Pair<Token, ASTType> var : boundVars) {
            ASTType type = var.snd();
            if(type != null) {
                addChild(type);
            }
        }
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getBinderToken() {
        return binderToken;
    }
    
    public final int countBoundVariabls() {
        return boundVars.size();
    }

    public final ASTType getVariableType(int index) {
        return boundVars.get(index).snd();
    }

    public final Token getVariableToken(int index) {
        return boundVars.get(index).fst();
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
