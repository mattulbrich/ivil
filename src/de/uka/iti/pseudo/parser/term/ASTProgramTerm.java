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
    private boolean schema;
    private Token position;
    
    private ASTProgramTerm(Token position, boolean terminating, boolean schema) {
        super(Collections.<ASTTerm>emptyList());
        this.terminating = terminating;
        this.schema = schema;
        this.position = position;
    }

    public ASTProgramTerm(Token label, boolean termination,
            ASTStatement matchStatement) {
        this(label, termination, true);
        if(matchStatement != null)
            addChild(matchStatement);
    }

    public ASTProgramTerm(Token label, boolean termination,
            List<ASTProgramUpdate> list) {
        this(label, termination, false);
        addChildren(list);
    }

    @Override
	public Token getLocationToken() {
    	return position;
	}
    
    public Token getPosition() {
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
        return schema && getChildren().size() > 0;
    }

    public ASTStatement getMatchingStatement() {
        assert hasMatchingStatement();
        return (ASTStatement) getChildren().get(0);
    }

    public boolean isSchema() {
        return schema;
    }

}
