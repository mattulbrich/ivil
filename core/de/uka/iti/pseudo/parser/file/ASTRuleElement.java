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
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.Token;

public abstract class ASTRuleElement extends ASTElement {

	protected Token firstToken;

	public ASTRuleElement(Token first) {
		this.firstToken = first;
	}

    public Token getFirstToken() {
        return firstToken;
    }
    
    public Token getLocationToken() {
        return getFirstToken();
    }
}
