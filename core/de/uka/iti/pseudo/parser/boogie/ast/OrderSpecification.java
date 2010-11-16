package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class OrderSpecification extends ASTElement {

    private final Token first;
    private final List<OrderSpecParent> parents;
    private final boolean complete;

    public OrderSpecification(Token first, List<OrderSpecParent> parents, boolean complete) {
        this.first = first;
        this.parents = parents;
        this.complete = complete;

        addChildren(parents);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

    public boolean isComplete() {
        return complete;
    }

    public List<OrderSpecParent> getParents() {
        return parents;
    }

}
