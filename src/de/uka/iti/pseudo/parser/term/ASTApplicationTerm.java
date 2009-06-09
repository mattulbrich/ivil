package de.uka.iti.pseudo.parser.term;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTApplicationTerm extends ASTTerm {
    
    private Token functionSymbol;

    public ASTApplicationTerm(Token functionSymbol, List<ASTTerm> subterms) {
        super(subterms);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getFunctionSymbol() {
        return functionSymbol;
    }
    
}
