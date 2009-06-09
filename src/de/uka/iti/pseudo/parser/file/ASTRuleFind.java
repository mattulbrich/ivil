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

public class ASTRuleFind extends ASTRuleElement {

	private MatchingLocation matchingLocation;
	private ASTRawTerm rawTerm;

	public ASTRuleFind(Token t, ASTRawTerm rawTerm, MatchingLocation matchingLocation) {
		super(t);
		this.rawTerm = rawTerm;
		this.matchingLocation = matchingLocation;
		
		addChild(rawTerm);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}
	
	@Override
	protected Token getLocationToken() {
		return rawTerm.getLocationToken();
	}

}
