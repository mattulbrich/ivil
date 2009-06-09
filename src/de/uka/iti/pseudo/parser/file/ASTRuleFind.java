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

	public ASTRuleFind(Token t, ASTLocatedTerm locatedTerm) {
		super(t);
		addChild(locatedTerm);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}
	
	@Override
	protected Token getLocationToken() {
		return getFirstToken();
	}
	
    public MatchingLocation getMatchingLocation() {
        return getLocatedTerm().getMatchingLocation();
    }

    public ASTLocatedTerm getLocatedTerm() {
        return (ASTLocatedTerm) getChildren().get(0);
    }
    
}
