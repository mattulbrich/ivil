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

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTMapType;

public class ASTSortDeclaration extends ASTElement {

    private List<Token> typeVariables;
    private ASTMapType alias;
    private Token name;

    public ASTSortDeclaration(Token name, List<Token> tyvars, ASTMapType alias) {
        this.name = name;
        this.typeVariables = tyvars;
        this.alias = alias;

        if (isMapAlias())
            addChild(alias);
    }

    public List<Token> getTypeVariables() {
        return Collections.unmodifiableList(typeVariables);
    }

    public Token getName() {
        return name;
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

	public Token getLocationToken() {
    	return name;
	}

    public boolean isMapAlias() {
        return null != alias;
    }

    public ASTMapType getAlias() {
        assert isMapAlias() : "ensure to call this only if you checked the presence of an alias";
        return alias;
    }
}
