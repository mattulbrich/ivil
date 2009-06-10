package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;

public class ASTProgramUpdate extends ASTElement {

    public ASTProgramUpdate(ASTLiteralLabel position, ASTStatement statement) {
        addChild(position);
        addChild(statement);
    }

    @Override public Token getLocationToken() {
        return getChildren().get(0).getLocationToken();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    public ASTLiteralLabel getPosition() {
        return (ASTLiteralLabel) getChildren().get(0);
    }
    
    public ASTStatement getStatement() {
        return (ASTStatement) getChildren().get(1);
    }
}
