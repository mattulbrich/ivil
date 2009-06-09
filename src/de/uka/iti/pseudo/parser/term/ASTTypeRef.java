package de.uka.iti.pseudo.parser.term;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTTypeRef extends ASTElement {
    
    private Token token;
    private List<ASTTypeRef> argumentTypeRefs;

    public ASTTypeRef(Token token, List<ASTTypeRef> argumentTypeRefs) {
        this.token = token;
        this.argumentTypeRefs = argumentTypeRefs;
        
        addChildren(argumentTypeRefs);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return token;
	}

}
