package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class ForallExpression extends Expression {

    private final QuantifierBody body;

    public ForallExpression(Token first, QuantifierBody body) {
        super(first);

        this.body = body;

        addChild(body);
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

    public QuantifierBody getBody() {
        return body;
    }

}
