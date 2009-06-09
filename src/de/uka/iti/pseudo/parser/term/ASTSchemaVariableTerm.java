package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import nonnull.NonNull;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTSchemaVariableTerm extends ASTTerm {
    
    private Token schemaToken;

    public ASTSchemaVariableTerm(Token t) {
        super(Collections.<ASTTerm>emptyList());
        schemaToken = t;
    }

    @Override protected Token getLocationToken() {
        return schemaToken;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    /**
     * return the name of this schema variable with the leading "%".
     * @return a string of positive length 
     */
    public @NonNull String getName() {
        return schemaToken.image;
    }

    public Token getToken() {
        return schemaToken;
    }

}
