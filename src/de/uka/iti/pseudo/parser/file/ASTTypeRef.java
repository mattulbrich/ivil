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

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTTypeRef extends ASTType {

    private List<ASTType> argTypes;
    private Token typeToken;

    public ASTTypeRef(Token token, List<ASTType> args) {
        this.typeToken = token;
        this.argTypes = args;

        addChildren(args);
    }

    public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<ASTType> getArgTypes() {
        return Collections.unmodifiableList(argTypes);
    }

    public Token getTypeToken() {
        return typeToken;
    }

	protected Token getLocationToken() {
		return typeToken;
	}

}
