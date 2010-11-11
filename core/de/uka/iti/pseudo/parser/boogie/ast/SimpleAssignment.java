package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class SimpleAssignment extends ASTElement {
    public final Token name;
    public final List<List<Expression>> arrayArgs; // e.g. [expr, expr][expr][]
                                                   // -> {{expr, expr},{expr},
                                                   // {}}
    public final Expression newVal;

    public SimpleAssignment(Token name, List<List<Expression>> args, Expression val) {
        this.name = name;
        this.arrayArgs = args;
        this.newVal = val;

        for (List<Expression> L : arrayArgs)
            addChildren(L);

        addChild(val);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return name;
    }
}