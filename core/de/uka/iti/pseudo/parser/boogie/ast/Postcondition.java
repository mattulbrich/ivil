package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class Postcondition extends Specification {

    private final boolean isFree;
    private final List<Attribute> attributes;
    private final Expression condition;

    public Postcondition(Token first, boolean isFree, List<Attribute> attr, Expression expr) {
        super(first);

        this.isFree = isFree;
        this.attributes = attr;
        this.condition = expr;

        addChildren(attributes);
        addChild(condition);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean isFree() {
        return isFree;
    }
}
