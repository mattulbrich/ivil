package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTWhereClause extends ASTRuleElement {
    
    Token identifier;

    public ASTWhereClause(Token headToken, Token t, List<ASTRawTerm> args) {
        super(headToken);
        identifier = t;
    }

    @Override 
    protected Token getLocationToken() {
        return identifier;
    }

    @Override public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getIdentifier() {
        return identifier;
    }

}
