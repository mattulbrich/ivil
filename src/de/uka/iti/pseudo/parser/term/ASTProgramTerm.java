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

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;

public class ASTProgramTerm extends ASTTerm {

    private boolean terminating;
    private ASTProgramLabel position;
    private boolean withMatchingStatement;
    
    private ASTProgramTerm(ASTProgramLabel position, boolean terminating) {
        super(Collections.<ASTTerm>emptyList());
        this.terminating = terminating;
        this.position = position;
    }

    public ASTProgramTerm(ASTProgramLabel position, boolean termination,
            ASTStatement matchStatement) {
        this(position, termination);
        this.withMatchingStatement = true;
        addChild(matchStatement);
    }

    public ASTProgramTerm(ASTProgramLabel position, boolean termination,
            List<ASTProgramUpdate> list) {
        this(position, termination);
        this.withMatchingStatement = false;
        addChildren(list);
    }

    @Override
	public Token getLocationToken() {
    	return position.getLocationToken();
	}
    
    public boolean isTerminating() {
        return terminating;
    }

    @Override 
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean hasMatchingStatement() {
        return withMatchingStatement;
    }

    public ASTStatement getMatchingStatement() {
        assert hasMatchingStatement();
        return (ASTStatement) getChildren().get(0);
    }

}
