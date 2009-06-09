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

public class ASTModCompound extends ASTModality {
    
    public ASTModCompound(List<ASTModality> list) {
        assert list.size() > 1;
        addChildren(list);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return getModality(0).getLocationToken();
	}

    public ASTModality getModality(int i) {
        return (ASTModality) getChildren().get(i);
    }

}
