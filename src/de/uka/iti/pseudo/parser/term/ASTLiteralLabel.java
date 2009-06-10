package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTLiteralLabel extends ASTProgramLabel {

    private Token literalToken;
    
    public ASTLiteralLabel(Token t) {
        literalToken = t;
    }

    @Override public Token getLocationToken() {
        return literalToken;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getLiteralToken() {
        return literalToken;
    }

}
