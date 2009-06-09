/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTTypeVar extends ASTType {

    private Token typeVarToken;

    public ASTTypeVar(Token token) {
        this.typeVarToken = token;
        assert token.image.charAt(0) == '\'';
    }

    public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getTypeVarToken() {
        return typeVarToken;
    }

	protected Token getLocationToken() {
		return typeVarToken;
	}

}
