package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModSchema extends ASTModality {
    
    private Token schemaIdentifier;

    public ASTModSchema(Token id) {
        schemaIdentifier = id;
    }

    @Override 
    protected Token getLocationToken() {
        return schemaIdentifier;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getSchemaIdentifier() {
        return schemaIdentifier;
    }

}
