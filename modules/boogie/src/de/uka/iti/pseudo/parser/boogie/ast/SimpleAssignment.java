package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class SimpleAssignment extends ASTElement {
    public final Token name;

    public final Expression target;

    public final Expression newVal;

    // args are in form of e.g. [expr,expr][expr][] and will be translatet to
    // mapaccessexpressions like {{expr, expr},{expr},{}}
    public SimpleAssignment(Token name, Expression target, Expression val) {
        this.name = name;
        this.newVal = val;
        this.target = target;

        addChild(target);
        addChild(val);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Expression getTarget() {
        return target;
    }

    public Expression getNewValue() {
        return newVal;
    }

    @Override
    public Token getLocationToken() {
        return name;
    }

    @Override
    public String toString() {
        return "SimpleAssignment [" + name.image + ", " + getLocation() + "]";
    }
}