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

public class ASTBinderDeclaration extends ASTElement {

    private ASTType variableType;

    private ASTType rangeType;

    private List<ASTType> typeReferenceList;

    private Token name;

    public ASTBinderDeclaration(Token name, ASTType range, ASTType varty,
            List<ASTType> tyrefs) {
        this.name = name;
        this.rangeType = range;
        this.variableType = varty;
        this.typeReferenceList = tyrefs;

        addChild(range);
        addChild(varty);
        addChildren(tyrefs);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTType getVariableType() {
        return variableType;
    }

    public ASTType getRangeType() {
        return rangeType;
    }

    public List<ASTType> getTypeReferenceList() {
        return Collections.unmodifiableList(typeReferenceList);
    }

    public Token getName() {
        return name;
    }

	public Token getLocationToken() {
		return name;
	}

}
