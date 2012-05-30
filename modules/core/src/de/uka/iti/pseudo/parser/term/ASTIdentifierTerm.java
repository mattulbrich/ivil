/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTIdentifierTerm extends ASTTerm {
    
    private Token symbol;

    public ASTIdentifierTerm(Token symbol) {
        super(Collections.<ASTTerm>emptyList());
        this.symbol = symbol;
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getSymbol() {
        return symbol;
    }
    
    @Override
	public Token getLocationToken() {
    	return symbol;
	}
    
    @Override
    public String toString() {
    	return super.toString() + "[" + symbol + "]";
    }

}
