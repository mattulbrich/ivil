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

import de.uka.iti.pseudo.parser.Token;

public abstract class ASTProgramTerm extends ASTTerm {

    private boolean terminating;
    private ASTProgramLabel position;
    
    public ASTProgramTerm(ASTProgramLabel position, boolean terminating) {
        super(Collections.<ASTTerm>emptyList());
        this.terminating = terminating;
        this.position = position;
    }

    @Override
	public Token getLocationToken() {
    	return position.getLocationToken();
	}
    
    public boolean isTerminating() {
        return terminating;
    }


}
