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
package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.ParserConstants;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;

public class ASTProgramTerm extends ASTTerm {

    private boolean terminating;
    private Token position;
    private @Nullable Token programReference;
    
    private ASTProgramTerm(Token position, boolean terminating) {
        super(Collections.<ASTTerm>emptyList());
        this.terminating = terminating;
        this.position = position;
	this.programReference = null;
    }

    public ASTProgramTerm(Token label, boolean termination,
            ASTStatement matchStatement) {
        this(label, termination);
        if(matchStatement != null)
            addChild(matchStatement);
    }

    public ASTProgramTerm(Token label, boolean termination,
            Token programReference) {
        this(label, termination);
        this.programReference = programReference;
    }

    @Override
	public Token getLocationToken() {
    	return position;
	}
    
    public Token getProgramReferenceToken() {
        assert !isSchema() : "Schema programs have no such token";
        assert programReference != null : "nullness";
        return programReference;
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
