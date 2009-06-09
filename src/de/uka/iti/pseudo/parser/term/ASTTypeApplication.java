/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTTypeApplication extends ASTType {
    
    private Token token;
    private List<ASTType> argumentTypeRefs;

    public ASTTypeApplication(Token token, List<ASTType> argumentTypeRefs) {
        this.token = token;
        this.argumentTypeRefs = argumentTypeRefs;
        
        addChildren(argumentTypeRefs);
    }
    
    public Token getTypeToken() {
		return token;
	}

	public List<ASTType> getArgumentTypeRefs() {
		return argumentTypeRefs;
	}

	@Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return token;
	}

}
