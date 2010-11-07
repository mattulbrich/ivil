package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.Token;

public abstract class DeclarationBlock extends ASTElement {

    protected Token firstToken;

    public DeclarationBlock(Token firstToken) {
        super();
        this.firstToken = firstToken;
    }

    @Override
    public Token getLocationToken() {
        return firstToken;
    }
}
