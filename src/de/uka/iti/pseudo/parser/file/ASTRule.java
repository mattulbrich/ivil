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

public class ASTRule extends ASTDeclarationBlock {

	private Token name;
	private List<ASTRuleElement> ruleElements;

	public ASTRule(Token first, Token name, List<ASTRuleElement> list, List<ASTGoalAction> actions) {
		super(first);
		this.name = name;
		this.ruleElements = list;
		
		addChildren(ruleElements);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

    public Token getName() {
        return name;
    }

}
