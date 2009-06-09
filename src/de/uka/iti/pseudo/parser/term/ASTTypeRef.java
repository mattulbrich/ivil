package de.uka.iti.pseudo.parser.term;

import java.util.Collections;
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
    
    public Token getTypeToken() {
		return token;
	}

	public List<ASTTypeRef> getArgumentTypeRefs() {
		return Collections.unmodifiableList(argumentTypeRefs);
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
