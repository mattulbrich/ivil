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

import java.util.Collections;
import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.ParserConstants;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.term.Modality;

public class ASTProgramTerm extends ASTTerm {

    private Token position;
    private @Nullable Token programReference;
    private Modality modality;
    
    private ASTProgramTerm(Token position, Modality modality) {
        super(Collections.<ASTTerm>emptyList());
        this.modality = modality;
        this.position = position;
        this.programReference = null;
    }

    public ASTProgramTerm(Token label, Modality modality,
            ASTStatement matchStatement) {
        this(label, modality);
        if(matchStatement != null)
            addChild(matchStatement);
    }

    public ASTProgramTerm(Token label, Modality modality,
            Token programReference) {
        this(label, modality);
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
    
    public Modality getModality() {
        return modality;
    }

    @Override 
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean hasMatchingStatement() {
        return isSchema() && getChildren().size() > 1;
    }

    public ASTStatement getMatchingStatement() {
        assert hasMatchingStatement();
        return (ASTStatement) getChildren().get(0);
    }

    public boolean isSchema() {
        return position.kind == ParserConstants.SCHEMA_IDENTIFIER;
    }

    public ASTTerm getSuffixFormula() {
        List<ASTTerm> children = getSubterms();
        int size = children.size();
        if(size > 0) {
            return children.get(size - 1);
        } else {
            throw new IllegalStateException("There is no suffix formula! (Should not be parsed!)");
        }
    }

}
