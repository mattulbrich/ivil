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
import de.uka.iti.pseudo.parser.boogie.ast.expression.WildcardExpression;

public final class WhileStatement extends Statement implements NamedASTElement {

    private final Expression guard;
    private final List<LoopInvariant> invariants;
    private final List<Statement> body;

    public WhileStatement(Token first, Expression guard, List<LoopInvariant> invariants, List<Statement> body) {
        super(first);
        this.guard = guard;
        this.invariants = invariants;
        this.body = body;


        addChild(guard);
        addChildren(invariants);
        addChildren(body);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<LoopInvariant> getInvariants() {
        return invariants;
    }

    public List<Statement> getBody() {
        return body;
    }

    public boolean hasWildcardGuard() {
        return guard instanceof WildcardExpression;
    }

    public Expression getGuard() {
        assert null != guard;
        return guard;
    }

    @Override
    public String getName() {
        return "while-statment";
    }

}
