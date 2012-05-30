/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.Token;

public abstract class ASTDeclarationBlock extends ASTElement {

	protected Token firstToken;

	public ASTDeclarationBlock(Token firstToken) {
		super();
		this.firstToken = firstToken;
	}
	
	@Override
	public Token getLocationToken() {
		return firstToken;
	}

}
