package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class WhileStatement extends Statement {

    private final Expression guard;
    private final List<LoopInvariant> invariants;
    private final List<Statement> body;

    public WhileStatement(Token first, Expression guard, List<LoopInvariant> invariants, List<Statement> body) {
        super(first);
        this.guard = guard;
        this.invariants = invariants;
        this.body = body;


        addChild(guard);
        addChildren(invariants);
        addChildren(body);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<LoopInvariant> getInvariants() {
        return invariants;
    }

    public List<Statement> getBody() {
        return body;
    }

    public boolean hasWildcardGuard() {
        return guard instanceof WildcardExpression;
    }

    public Expression getGuard() {
        assert null != guard;
        return guard;
    }

}
