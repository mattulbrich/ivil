package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class AssumptionStatement extends Statement {

    private final Expression assumption;

    public AssumptionStatement(Token first, Expression expr) {
        super(first);
        this.assumption = expr;

        addChild(assumption);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Expression getAssertion() {
        return assumption;
    }

}
