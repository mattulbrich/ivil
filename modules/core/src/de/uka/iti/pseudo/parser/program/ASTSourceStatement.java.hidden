package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTSourceStatement extends ASTStatement {
    
    private Token argumentToken;

    public ASTSourceStatement(Token firstToken, Token argument) {
        super(firstToken);
        this.argumentToken = argument;
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getArgumentToken() {
        return argumentToken;
    }

}
