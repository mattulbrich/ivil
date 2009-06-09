package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTTypeVar extends ASTType {
    
    private Token typeVarToken;

    public ASTTypeVar(Token token) {
        typeVarToken = token;
    }

    @Override
    protected Token getLocationToken() {
        return getTypeVarToken();
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getTypeVarToken() {
        return typeVarToken;
    }

}
