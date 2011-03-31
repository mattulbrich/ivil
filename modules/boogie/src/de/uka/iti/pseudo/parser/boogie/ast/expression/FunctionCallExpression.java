package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class FunctionCallExpression extends Expression {

    private final String name;
    private final List<Expression> arguments;

    public FunctionCallExpression(Token name, List<Expression> args) {
        super(name);

        this.name = ASTConversions.getEscapedName(name);
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
