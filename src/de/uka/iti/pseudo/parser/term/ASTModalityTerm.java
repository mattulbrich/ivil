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

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModalityTerm extends ASTTerm {
    
    private ASTModality modality;

    public ASTModalityTerm(ASTModality modality, ASTTerm term) {
        super(Collections.<ASTTerm>singletonList(term));
        this.modality = modality;
        addChild(modality);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final ASTModality getModality() {
        return modality;
    }
    
    @Override
	protected Token getLocationToken() {
    	return modality.getLocationToken();
	}

}
