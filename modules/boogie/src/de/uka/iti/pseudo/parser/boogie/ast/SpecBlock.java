package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class SpecBlock extends ASTElement {

    private final List<Statement> body;

    public SpecBlock(List<Statement> body) {
        this.body = body;

        addChildren(body);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public SpecReturnStatement getReturnStatement() {
        Statement rval = body.get(body.size() - 1);
        if (rval instanceof SpecReturnStatement)
            return (SpecReturnStatement) rval;
        else
            return null;
    }

    @Override
    public Token getLocationToken() {
        return body.get(0).getLocationToken();
    }

}
