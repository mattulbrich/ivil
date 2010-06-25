package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTTypevarBinderTerm extends ASTTerm {
    
    Token binderToken;
    Token typeVarToken;

    public ASTTypevarBinderTerm(Token binderToken,
            Token typeVarToken, ASTTerm subterm) {
        super(Collections.singletonList(subterm));
        this.binderToken = binderToken;
        this.typeVarToken = typeVarToken;
    }

    @Override
    public Token getLocationToken() {
        return binderToken;
    }

    public Token getBinderToken() {
        return binderToken;
    }

    public Token getTypeVarToken() {
        return typeVarToken;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    public ASTTerm getTerm() {
        return getSubterms().get(0);
    }

}
