package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class WildcardExpression extends Expression {

    public WildcardExpression(Token first) {
        super(first);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public List<Expression> getOperands() {
        return new LinkedList<Expression>();
    }

}
