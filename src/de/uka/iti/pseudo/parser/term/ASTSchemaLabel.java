package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTProgramLabel;

public class ASTSchemaLabel extends ASTProgramLabel {
    
    private Token schemaToken;
    private boolean incremented;

    public ASTSchemaLabel(Token t, boolean incremented) {
        this.schemaToken = t;
        this.incremented = incremented;
    }

    @Override public Token getLocationToken() {
        return schemaToken;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getSchemaToken() {
        return schemaToken;
    }

    public boolean isIncremented() {
        return incremented;
    }

}
