/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

/**
 * This expression does something similar to loading a variable from a memory
 * into a register. This is needed to handle statements like "call i, m[i] :=
 * P(i, m[i])" correctly
 * 
 * @author timm.felden@felden.com
 */
public final class VariableUsageExpression extends Expression {

    private final String name;

    public VariableUsageExpression(Token first) {
        super(first);

        name = ASTConversions.getEscapedName(first);
    }

    @Override
    public List<Expression> getOperands() {
        assert false;
        return null;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }
}
