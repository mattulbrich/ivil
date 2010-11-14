package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.Token;

public abstract class Expression extends ASTElement {

    private final Token first;

    Expression(Token first) {
        this.first = first;
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

    public abstract List<Expression> getOperands();
}
