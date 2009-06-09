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
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTSortDeclarationBlock extends ASTDeclarationBlock {

	public ASTSortDeclarationBlock(Token first, List<ASTSortDeclaration> list) {
		super(first);
		addChildren(list);
	}

	public void visit(ASTVisitor v) throws ASTVisitException {
		v.visit(this);
	}

}
