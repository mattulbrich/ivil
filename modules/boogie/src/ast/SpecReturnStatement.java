package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;

public final class SpecReturnStatement extends Statement {

    private final Expression rval;

    public SpecReturnStatement(de.uka.iti.pseudo.parser.boogie.ast.Expression expr) {
        super(expr.getLocationToken());
        
        rval = expr;
        addChild(expr);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Expression getRval() {
        return rval;
    }

}
