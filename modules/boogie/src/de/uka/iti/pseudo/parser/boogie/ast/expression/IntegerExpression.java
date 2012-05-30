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

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class IntegerExpression extends Expression {

    private final List<Expression> operands = new LinkedList<Expression>();

    private final BigInteger value;

    public IntegerExpression(Token i) {
        super(i);
        
        value = new BigInteger(i.image);
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public BigInteger getValue() {
        return value;
    }
}
