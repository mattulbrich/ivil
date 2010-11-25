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

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTApplicationTerm extends ASTTerm {
    
    private Token functionToken;
    
    public ASTApplicationTerm(Token functionToken, List<ASTTerm> subterms) {
        super(subterms);
    	assert functionToken != null;
    	this.functionToken = functionToken;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getFunctionToken() {
        return functionToken;
    }
    
    @Override
	public Token getLocationToken() {
    	return functionToken;
	}

}
