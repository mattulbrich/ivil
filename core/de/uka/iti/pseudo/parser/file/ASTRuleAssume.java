/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTRuleAssume extends ASTRuleElement {

    public ASTRuleAssume(Token t, ASTLocatedTerm locatedTerm) {
        super(t);
        addChild(locatedTerm);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    private ASTLocatedTerm getLocatedTerm() {
        return (ASTLocatedTerm) getChildren().get(0);
    }
    
    
}
