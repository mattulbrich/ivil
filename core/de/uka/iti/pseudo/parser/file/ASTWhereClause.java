package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTWhereClause extends ASTRuleElement {
    
    Token identifier;

    public ASTWhereClause(Token headToken, Token t, List<ASTTerm> args) {
        super(headToken);
        this.identifier = t;
        addChildren(args);
    }

    public Token getLocationToken() {
        return identifier;
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getIdentifier() {
        return identifier;
    }

}