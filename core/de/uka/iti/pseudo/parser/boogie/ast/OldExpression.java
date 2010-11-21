package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class OldExpression extends Expression {

    // has exactly one operand
    private final List<Expression> operands;

    public OldExpression(Token first, Expression rval) {
        super(first);

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
