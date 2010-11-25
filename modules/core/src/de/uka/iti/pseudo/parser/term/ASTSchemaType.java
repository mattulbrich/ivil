package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTSchemaType extends ASTType {

    private Token schemaToken;
    
    public ASTSchemaType(Token token) {
        this.schemaToken = token;
    }

    @Override public Token getLocationToken() {
        return getSchemaTypeToken();
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getSchemaTypeToken() {
        return schemaToken;
    }

}
