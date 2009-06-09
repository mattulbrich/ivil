/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTBinderDeclarationBlock extends ASTDeclarationBlock {

	private List<ASTBinderDeclaration> declarationList;

	public ASTBinderDeclarationBlock(Token first, List<ASTBinderDeclaration> list) {
		super(first);
		this.declarationList = list;
		
		addChildren(list);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

}
