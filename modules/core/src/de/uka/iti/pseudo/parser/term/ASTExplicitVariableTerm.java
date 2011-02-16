package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTExplicitVariableTerm extends ASTTerm {

    private Token firstToken;
    private Token varToken;

    public Token getVarToken() {
        return varToken;
    }

    public ASTExplicitVariableTerm(Token firstToken, Token varToken) {
        super(Collections.<ASTTerm>emptyList());
        this.firstToken = firstToken;
        this.varToken = varToken;
    }

    @Override
    public Token getLocationToken() {
        return firstToken;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
