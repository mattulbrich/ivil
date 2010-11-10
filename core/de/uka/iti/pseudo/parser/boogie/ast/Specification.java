package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.Token;

public abstract class Specification extends ASTElement {

    private final Token first;

    Specification(Token first) {
        this.first = first;
    }

    @Override
    public Token getLocationToken() {
        return first;
    }
}
