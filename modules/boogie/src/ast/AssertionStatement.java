package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class AssertionStatement extends Statement {

    private final List<Attribute> attr;
    private final Expression assertion;

    public AssertionStatement(Token first, List<Attribute> attr, Expression expr) {
        super(first);
        this.attr = attr;
        this.assertion = expr;

        addChildren(attr);
        addChild(assertion);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<Attribute> getAttributes() {
        return attr;
    }

    public Expression getAssertion() {
        return assertion;
    }

}
