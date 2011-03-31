package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;

public final class BitvectorAccessSelectionExpression extends Expression {

    private final Expression target;
    private final List<Expression> operands;

    public BitvectorAccessSelectionExpression(Expression target, Expression args) {
        super(target.getLocationToken());
        
        this.operands = new ArrayList<Expression>(1);
        operands.add(args);

        this.target = target;

        addChild(target);
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

    public Expression getTarget() {
        return target;
    }

    public int getFirst() {
        return ((BitvectorSelectExpression) operands.get(0)).getFirst();
    }

    public int getLast() {
        return ((BitvectorSelectExpression) operands.get(0)).getLast();
    }
}
