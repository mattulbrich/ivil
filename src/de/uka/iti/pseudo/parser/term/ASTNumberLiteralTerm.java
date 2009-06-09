package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTNumberLiteralTerm extends ASTTerm {
    
    private Token numberToken;

    public ASTNumberLiteralTerm(Token t) {
        super(Collections.<ASTTerm>emptyList());
        numberToken = t;
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
}
