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

public class ASTRuleAssume extends ASTRuleElement {

	private ASTRawTerm rawTerm;
	private MatchingLocation matchingLocation;

	public ASTRuleAssume(Token first, ASTRawTerm rawTerm, MatchingLocation matchingLocation) {
		super(first);
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
