/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

public abstract class ASTRuleElement extends ASTFileElement {

	protected Token firstToken;

	public ASTRuleElement(Token first) {
		this.firstToken = first;
	}
}
