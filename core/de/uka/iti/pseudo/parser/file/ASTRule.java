/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.util.Pair;

public class ASTRule extends ASTDeclarationBlock {

	private Token name;
	private Token description;
    private List<Pair<Token, Token>> properties;

    public ASTRule(Token first, Token name, List<ASTRuleElement> list,
            List<ASTGoalAction> actions, Token description,
            List<Pair<Token, Token>> properties) {
        super(first);
        this.name = name;
        this.description = description;
        this.properties = properties;
        addChildren(list);
        addChildren(actions);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getName() {
        return name;
    }
    
    public Token getDescription() {
        return description;
    }

    public List<Pair<Token, Token>> getProperties() {
        return properties;
    }

}
