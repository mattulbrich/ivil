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

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

//TODO DOC

public class ASTHeadElement extends ASTElement {
    
    public ASTHeadElement(ASTElement element) {
        addChild(element);
    }

    @Override public Token getLocationToken() {
        throw new UnsupportedOperationException();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        throw new UnsupportedOperationException();
    }

    public ASTElement getWrappedElement() {
        return getChildren().get(0);
    }

}
