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
import de.uka.iti.pseudo.parser.ParserConstants;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;

public class ASTProgramTerm extends ASTTerm {

    private boolean terminating;
    private Token position;
    
    private ASTProgramTerm(Token position, boolean terminating) {
        super(Collections.<ASTTerm>emptyList());
        this.terminating = terminating;
        this.position = position;
    }

    public ASTProgramTerm(Token label, boolean termination,
            ASTStatement matchStatement) {
        this(label, termination);
        if(matchStatement != null)
            addChild(matchStatement);
    }

    public ASTProgramTerm(Token label, boolean termination,
            List<ASTProgramUpdate> list) {
        this(label, termination);
        addChildren(list);
    }

    @Override
	public Token getLocationToken() {
    	return position;
	}
    
    public Token getLabel() {
        return position;
    }
    
    public boolean isTerminating() {
        return terminating;
    }

    @Override 
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean hasMatchingStatement() {
        return isSchema() && getChildren().size() > 0;
    }

    public ASTStatement getMatchingStatement() {
        assert hasMatchingStatement();
        return (ASTStatement) getChildren().get(0);
    }

    public boolean isSchema() {
        return position.kind == ParserConstants.SCHEMA_IDENTIFIER;
    }

}
