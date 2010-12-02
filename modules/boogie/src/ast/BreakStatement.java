package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class BreakStatement extends Statement {

    private final String target;

    public BreakStatement(Token first, Token target) {
        super(first);
        this.target = null == target ? null : target.image;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean hasTarget() {
        return null != target;
    }

    public String getTarget() {
        assert null != target;
        return target;
    }
}
