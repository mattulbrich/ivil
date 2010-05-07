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

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

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
	public Token getLocationToken() {
    	return numberToken;
	}

	public Token getNumberToken() {
		return numberToken;
	}
    
	public String toString() {
		return super.toString() + "[" + numberToken + "]";
	}
}
