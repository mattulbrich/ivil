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

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTGotoStatement extends ASTStatement {

    public ASTGotoStatement(Token kw, List<ASTTerm> targets) {
        super(kw);
        addChildren(targets);
    }
    
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
