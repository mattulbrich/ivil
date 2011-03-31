package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class IfThenElseExpression extends Expression {

    private final Expression condition, _then, _else;
    private final List<Expression> operands;

    public IfThenElseExpression(Token first, Expression condition, Expression _then, Expression _else) {
        super(first);

        this.condition = condition;
        this._else = _else;
        this._then = _then;
        
        this.operands = new ArrayList<Expression>(3);
        operands.add(condition);
        operands.add(_else);
        operands.add(_then);

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

    public Expression getCondition() {
        return condition;
    }

    public Expression getThen() {
        return _then;
    }

    public Expression getElse() {
        return _else;
    }

}
