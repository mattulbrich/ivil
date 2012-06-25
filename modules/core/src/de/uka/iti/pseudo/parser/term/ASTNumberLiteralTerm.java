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

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

/**
 * The AST class for number literal terms generated by the
 * {@link de.uka.iti.pseudo.parser.Parser}.
 */
public class ASTNumberLiteralTerm extends ASTTerm {

    private final Token numberToken;

    // Checkstyle: IGNORE JavadocMethod - AST creation is obvious
    public ASTNumberLiteralTerm(Token t) {
        super(Collections.<ASTTerm> emptyList());
        numberToken = t;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return numberToken;
    }

    public Token getNumberToken() {
        return numberToken;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + numberToken + "]";
    }
}
