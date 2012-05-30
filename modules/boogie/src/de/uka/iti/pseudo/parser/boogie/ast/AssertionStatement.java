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

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;

public final class AssertionStatement extends Statement {

    private final List<Attribute> attr;
    private final Expression assertion;

    public AssertionStatement(Token first, List<Attribute> attr, Expression expr) {
        super(first);
        this.attr = attr;
        this.assertion = expr;

        addChildren(attr);
        addChild(assertion);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<Attribute> getAttributes() {
        return attr;
    }

    public Expression getAssertion() {
        return assertion;
    }

}
