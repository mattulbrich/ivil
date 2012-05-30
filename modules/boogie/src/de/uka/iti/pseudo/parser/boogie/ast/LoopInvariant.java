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

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;

public final class LoopInvariant extends ASTElement {

    private final boolean free;
    private final Expression expr;

    public LoopInvariant(boolean free, Expression expr) {
        this.free = free;
        this.expr = expr;

        addChild(expr);
    }

    @Override
    public Token getLocationToken() {
        return expr.getLocationToken();
    }

    public boolean isFree() {
        return free;
    }

    public Expression getExpression() {
        return expr;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
