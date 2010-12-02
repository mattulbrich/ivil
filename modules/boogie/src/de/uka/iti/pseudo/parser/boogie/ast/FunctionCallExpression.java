package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class FunctionCallExpression extends Expression {

    private final String name;
    private final List<Expression> arguments;

    public FunctionCallExpression(Token first, List<Expression> args) {
        super(first);

        name = first.image;
        arguments = args;

        addChildren(args);
    }

    @Override
    public List<Expression> getOperands() {
        return arguments;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

}
