package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.Token;

public abstract class ASTStatement extends ASTElement {
    
    protected Token firstToken;

    public ASTStatement(Token firstToken) {
        this.firstToken = firstToken;
    }
    
    public Token getLocationToken() {
        return firstToken;
    }

}
