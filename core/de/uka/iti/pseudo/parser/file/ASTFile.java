/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTFile extends ASTElement {
	
	protected final static boolean DEBUG = false;

	private List<ASTDeclarationBlock> declarationBlocks;

	// do not store program and problem here in persona
	// problem may be replaced if it is a fix expression.
	// -1 indicates not present
	private int problemIndex = -1;

	public ASTFile(List<ASTDeclarationBlock> blocks, ASTTerm problem) {

		this.declarationBlocks = blocks;
		addChildren(blocks);
		
		if(problem != null) {
		    problemIndex = getChildren().size();
			addChild(problem);
		}
	}

	public void visit(ASTVisitor v) throws ASTVisitException{
		v.visit(this);
	}

	public static boolean isDEBUG() {
		return DEBUG;
	}

	public List<ASTDeclarationBlock> getDeclarationBlocks() {
		return Collections.unmodifiableList(declarationBlocks);
	}

	public ASTTerm getProblemTerm() {
	    return (ASTTerm) (problemIndex > 0 ? getChildren().get(problemIndex) : null);
	}
	
	@Override
	public Token getLocationToken() {
		return null;
	}

}
