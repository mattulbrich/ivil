package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class RelationExpression extends Expression {

    private final List<Expression> operands;
    private final String function;

    public RelationExpression(Token loc, Expression rval, Expression tmp, String function) {
        super(loc);

        this.function = function;

        operands = new ArrayList<Expression>(2);
        operands.add(rval);
        operands.add(tmp);

        addChildren(operands);
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    public String getFunction() {
        return function;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}