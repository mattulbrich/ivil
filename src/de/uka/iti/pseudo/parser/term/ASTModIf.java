package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModIf extends ASTModality {
    
    private Token headToken;
    private ASTTerm conditionTerm;
    private ASTModality thenModality;
    private ASTModality elseModality;

    public ASTModIf(Token t, ASTTerm condTerm, ASTModality thenMod, ASTModality elseMod) {
        this.headToken = t;
        this.conditionTerm = condTerm;
        this.thenModality = thenMod;
        this.elseModality = elseMod;
        
        addChild(condTerm);
        addChild(thenMod);
        addChild(elseMod);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return headToken;
	}

}
