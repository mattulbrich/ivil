/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * A code block inside a code expression.
 * 
 * @author timm.felden@felden.com
 */
public final class CodeBlock extends ASTElement {

    private final List<Statement> body;

    public CodeBlock(List<Statement> body) {
        this.body = body;

        addChildren(body);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    /**
     * The boogie paper describing code expressions states that return
     * statements are the last statements of a block.
     * 
     * @return the return statement or null, if there is none
     */
    public CodeExpressionReturn getReturnStatement() {
        Statement rval = body.get(body.size() - 1);
        if (rval instanceof CodeExpressionReturn)
            return (CodeExpressionReturn) rval;
        else
            return null;
    }

    @Override
    public Token getLocationToken() {
        return body.get(0).getLocationToken();
    }

}
