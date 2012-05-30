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

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTHavocStatement extends ASTStatement {

    public ASTHavocStatement(Token kw, ASTTerm term) {
        super(kw);
        addChild(term);
        
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTTerm getArgument() {
        return (ASTTerm) getChildren().get(0);
    }

}
