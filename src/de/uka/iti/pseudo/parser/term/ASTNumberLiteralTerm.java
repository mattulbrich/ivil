/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTNumberLiteralTerm extends ASTTerm {
    
    private Token numberToken;

    public ASTNumberLiteralTerm(Token t) {
        super(Collections.<ASTTerm>emptyList());
        numberToken = t;
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return numberToken;
	}

	public Token getNumberToken() {
		return numberToken;
	}
    
	public String toString() {
		return super.toString() + "[" + numberToken + "]";
	}
}
