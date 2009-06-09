package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModWhile extends ASTModality {

    private Token headToken;
    private ASTTerm conditionTerm;
    private ASTModality bodyModality;
    
    public ASTModWhile(Token t, ASTTerm condTerm, ASTModality bodyMod) {
        this.headToken = t;
        this.conditionTerm = condTerm;
        this.bodyModality = bodyMod;
        
        addChild(condTerm);
        addChild(bodyMod);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
