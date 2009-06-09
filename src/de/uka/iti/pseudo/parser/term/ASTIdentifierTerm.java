package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTIdentifierTerm extends ASTTerm {
    
    private Token symbol;

    public ASTIdentifierTerm(Token symbol) {
        super(Collections.<ASTTerm>emptyList());
        this.symbol = symbol;
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getSymbol() {
        return symbol;
    }

}
