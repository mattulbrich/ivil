/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTWhereClause extends ASTRuleElement {
    
    private Token identifier;
    private boolean inverted;

    public ASTWhereClause(Token headToken, boolean inverted, Token t, List<ASTTerm> args) {
        super(headToken);
        this.identifier = t;
        this.inverted = inverted;
        addChildren(args);
    }

    @Override
    public Token getLocationToken() {
        return identifier;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getIdentifier() {
        return identifier;
    }
    
    public boolean isInverted() {
        return inverted;
    }

}
