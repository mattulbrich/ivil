/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import java.util.Collections;
import java.util.List;

import nonnull.Nullable;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;

public class ASTProgramTerm extends ASTTerm {

    private boolean terminating;
    
    public ASTProgramTerm(boolean terminating, ASTNumberLiteralTerm position,
            ASTStatement statement) {
        super(Collections.<ASTTerm>emptyList());
        this.terminating = terminating;
        
        super.addChild(position);
        if(statement != null)
            super.addChild(statement);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
	public Token getLocationToken() {
    	return getChildren().get(0).getLocationToken();
	}
    
    public boolean isTerminating() {
        return terminating;
    }

    /*
     * statement is optionally there as second child element 
     */
    public @Nullable ASTStatement getStatement() {
        List<ASTElement> children = getChildren();
        if(children.size() > 0)
            return (ASTStatement) children.get(1);
        else
            return null;
    }

}
