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
import de.uka.iti.pseudo.parser.term.ASTType;

public class ASTFunctionDeclaration extends ASTElement {

    private ASTType rangeType;

    private List<ASTType> argumentTypes;

    private Token precedence;

    private Token modifier;

    private Token name;
    
	private Token operatorIdentifier;

    public ASTFunctionDeclaration(ASTType range, Token name,
            List<ASTType> tyrefs, Token modifier,
            Token operatorIdentifier, Token precedence) {
    	this.rangeType = range;
    	this.argumentTypes = tyrefs;
    	this.name = name;
    	this.modifier = modifier;
    	this.operatorIdentifier = operatorIdentifier;
    	this.precedence = precedence;
    	addChild(range);
    	addChildren(tyrefs);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTType getRangeType() {
        return rangeType;
    }

    public List<ASTType> getArgumentTypes() {
        return Collections.unmodifiableList(argumentTypes);
    }

    public Token getPrecedence() {
        return precedence;
    }

    public Token getName() {
        return name;
    }

    public boolean isInfix() {
        return modifier != null && modifier.image.equals("infix");
    }
    
    public boolean isPrefix() {
        return modifier != null && modifier.image.equals("prefix");
    }
    
    public boolean isAssignable() {
        return modifier != null && modifier.image.equals("assignable");
    }

    public Token getOperatorIdentifier() {
		return operatorIdentifier;
	}

	public Token getLocationToken() {
        return name;
    }

	public boolean isUnique() {
		return modifier != null && modifier.image.equals("unique");
	}

}
