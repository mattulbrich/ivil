/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTRawTerm extends ASTFileElement {
	
	private Token termToken;
    private ASTTerm termAST;

	public ASTRawTerm(Token token) {
		this.termToken = token;
		
		assert termToken.kind == FileParser.TERM;
	}
 
	public void visit(ASTFileVisitor v)  throws ASTVisitException {
		v.visit(this);
	}

	public Token getTermToken() {
		return termToken;
	}

	protected Token getLocationToken() {
		return termToken;
	}

    public void setTermAST(ASTTerm ast) {
        termAST = ast;
    }

    public ASTTerm getTermAST() {
        return termAST;
    }
    
}
