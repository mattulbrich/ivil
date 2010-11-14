package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class UnaryMinusExpression extends Expression {

    private final List<Expression> operands;

    public UnaryMinusExpression(Token loc, Expression rval) {
        super(loc);

        operands = new ArrayList<Expression>(1);
        operands.add(rval);

        addChildren(operands);
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
