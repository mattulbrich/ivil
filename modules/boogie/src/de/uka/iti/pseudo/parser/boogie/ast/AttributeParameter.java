package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;

public final class AttributeParameter extends ASTElement {

    private final Token location;

    private final String flagName;
    private final Expression arg;

    public AttributeParameter(Token t) {
        location = t;

        flagName = t.image;
        this.arg = null;
    }

    public AttributeParameter(Expression expr) {
        location = expr.getLocationToken();

        arg = expr;
        flagName = null;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    public boolean isFlag() {
        return null != flagName;
    }

    public Expression getExpression() {
        assert arg != null;
        return arg;
    }

}
