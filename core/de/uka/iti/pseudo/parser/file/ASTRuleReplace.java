/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTRuleReplace extends ASTRuleElement {

	private ASTTerm rawTerm;

	public ASTRuleReplace(Token first, ASTTerm rawTerm) {
		super(first);
		this.rawTerm = rawTerm;
		addChild(rawTerm);
	}

	public void visit(ASTVisitor v) throws ASTVisitException {
		v.visit(this);
	}
	
	@Override
	public Token getLocationToken() {
		return rawTerm.getLocationToken();
	}
	
	public ASTTerm getTerm() {
	    return (ASTTerm) getChildren().get(0);
	}

}
