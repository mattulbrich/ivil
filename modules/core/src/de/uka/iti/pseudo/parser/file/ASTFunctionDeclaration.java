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

    private int modifier;
    
    public static final int MODIFIER_UNIQUE = 1;
    public static final int MODIFIER_ASSIGNABLE = 2;
    public static final int MODIFIER_PREFIX = 4;
    public static final int MODIFIER_INFIX = 8;

    private Token name;
    
	private Token operatorIdentifier;

    public ASTFunctionDeclaration(ASTType range, Token name,
            List<ASTType> tyrefs, int modifier,
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
        return (modifier & MODIFIER_INFIX) != 0;
    }
    
    public boolean isPrefix() {
        return (modifier & MODIFIER_PREFIX) != 0;
    }
    
    public boolean isAssignable() {
        return (modifier & MODIFIER_ASSIGNABLE) != 0;
    }
    
    public boolean isUnique() {
        return (modifier & MODIFIER_UNIQUE) != 0;
    }


    public Token getOperatorIdentifier() {
		return operatorIdentifier;
	}

	public Token getLocationToken() {
        return name;
    }
}