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
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTProgramTerm extends ASTTerm {

    private boolean terminating;
    
    public ASTProgramTerm(boolean terminating, ASTNumberLiteralTerm position) {
        super(Collections.<ASTTerm>emptyList());
        this.terminating = terminating;
        super.addChild(position);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
	public Token getLocationToken() {
    	return getChildren().get(0).getLocationToken();
	}

}
