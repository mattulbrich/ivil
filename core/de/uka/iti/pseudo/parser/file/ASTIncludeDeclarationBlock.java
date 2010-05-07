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

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.ParserConstants;
import de.uka.iti.pseudo.parser.Token;

public class ASTIncludeDeclarationBlock extends ASTDeclarationBlock {

	private List<Token> includeStrings;

	public ASTIncludeDeclarationBlock(Token first, List<Token> list) {
		super(first);
		
		includeStrings = list;
		
		// TODO really needed?!
		assert onlyStringTokens();
	}

	private boolean onlyStringTokens() {
		for (Token token : includeStrings) {
			if(token.kind != ParserConstants.STRING) {
				System.err.println("Unexpected token " + token + " (" + token.kind + ")");
				return false;
			}
		}
		return true;
	}

	public void visit(ASTVisitor v) throws ASTVisitException {
		v.visit(this);
	}

	public List<Token> getIncludeStrings() {
		return Collections.unmodifiableList(includeStrings);
	}

}
