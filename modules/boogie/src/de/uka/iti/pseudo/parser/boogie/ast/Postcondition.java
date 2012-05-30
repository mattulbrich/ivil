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

public final class Postcondition extends Specification {

    private final boolean isFree;
    private final List<Attribute> attributes;
    private final Expression condition;

    public Postcondition(Token first, boolean isFree, List<Attribute> attr, Expression expr) {
        super(first);

        this.isFree = isFree;
        this.attributes = attr;
        this.condition = expr;

        addChildren(attributes);
        addChild(condition);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Expression getCondition() {
        return condition;
    }

    public boolean isFree() {
        return isFree;
    }
}
