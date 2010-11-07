package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class FunctionDeclaration extends DeclarationBlock {

    private final List<Attribute> attributes;
    private final String name;

    // ! @note: can be null if a function has no specification
    private final Expression expression;

    public FunctionDeclaration(Token firstToken, List<Attribute> attributes /*
                                                                             * insert
                                                                             * signature
                                                                             * arguments
                                                                             * here
                                                                             */, Token name, Expression expression) {
        super(firstToken);
        this.attributes = attributes;
        this.name = name.image;
        this.expression = expression;

        addChildren(attributes);
        if (null != expression)
            addChild(expression);
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public String getName() {
        return name;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
}
