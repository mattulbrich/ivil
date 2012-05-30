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

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;

public final class AssumptionStatement extends Statement {

    private final Expression assumption;

    public AssumptionStatement(Token first, Expression expr) {
        super(first);
        this.assumption = expr;

        addChild(assumption);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Expression getAssertion() {
        return assumption;
    }

}
