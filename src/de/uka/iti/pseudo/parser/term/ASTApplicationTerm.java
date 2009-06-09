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

public class ASTApplicationTerm extends ASTTerm {
    
    private Token functionSymbol;

    public ASTApplicationTerm(Token functionSymbol, List<ASTTerm> subterms) {
        super(subterms);
    	assert functionSymbol != null;
    	this.functionSymbol = functionSymbol;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getFunctionSymbol() {
        return functionSymbol;
    }
    
    @Override
	protected Token getLocationToken() {
    	return functionSymbol;
	}
    
}
