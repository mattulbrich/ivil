package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;

/**
 * Coercions behave a bit like checked typecasts; they enforce, that an
 * expression expr:type is of Type type, but they dont do any value changing
 * casting. Therefore, if the expression is not already of this type, the
 * coercion fails. Unfortunately nowhere is stateted, what a failing coercion
 * really means. This type of expression is usually used to instantiate type
 * parameters in a way, that allows for automated proving of problems.
 * 
 * @author timm.felden@felden.com
 */
public final class CoercionExpression extends Expression {

    private final Type type;
    private final List<Expression> operands;

    public CoercionExpression(Expression expr, Type type) {
        super(type.getLocationToken());

        operands = new LinkedList<Expression>();
        operands.add(expr);
        this.type = type;

        addChild(expr);
        addChild(type);
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Type getType() {
        return type;
    }

}
