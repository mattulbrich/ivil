/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

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
    
    @Override public Token getLocationToken() {
    	return token;
	}

}
