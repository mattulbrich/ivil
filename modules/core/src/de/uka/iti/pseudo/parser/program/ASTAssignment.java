/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTAssignment extends ASTElement {

    public ASTAssignment(ASTTerm target, ASTTerm term) {
        addChild(target);
        addChild(term);
    }
    
    public ASTTerm getTarget() {
        return (ASTTerm) getChildren().get(0);
    }
    
    public boolean isSchema() {
        return getTarget() instanceof ASTSchemaVariableTerm;
    }
    
    /**
     * get the value term which is assigned. This is the second child in the AST.
     * @return the assigned term as AST.
     */
    public ASTTerm getTerm() {
        return (ASTTerm) getChildren().get(1);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getLocationToken() {
        return getTarget().getLocationToken();
    }

}
