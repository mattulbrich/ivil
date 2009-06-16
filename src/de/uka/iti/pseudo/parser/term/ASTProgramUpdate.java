package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;

public class ASTProgramUpdate extends ASTElement {
    
    private Token position;

    public ASTProgramUpdate(Token position, ASTStatement statement) {
        this.position = position;
        addChild(statement);
    }

    @Override public Token getLocationToken() {
        return getChildren().get(0).getLocationToken();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    public Token getPosition() {
        return position;
    }
    
    public ASTStatement getStatement() {
        return (ASTStatement) getChildren().get(0);
    }
}
