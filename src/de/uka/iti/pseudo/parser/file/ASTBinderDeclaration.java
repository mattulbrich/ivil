/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTBinderDeclaration extends ASTFileElement {

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

    public void visit(ASTFileVisitor v) throws ASTVisitException {
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

	protected Token getLocationToken() {
		return name;
	}

}
