package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;

public final class BitvectorAccessSelectionExpression extends Expression {

    private final List<Expression> operands;

    public BitvectorAccessSelectionExpression(Expression tmp) {
        super(tmp.getLocationToken());
        
        this.operands = new ArrayList<Expression>(1);
        operands.add(tmp);

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
