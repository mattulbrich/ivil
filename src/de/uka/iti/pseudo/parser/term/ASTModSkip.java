package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModSkip extends ASTModality {
    
    private Token headToken;

    public ASTModSkip(Token t) {
        headToken = t;
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
