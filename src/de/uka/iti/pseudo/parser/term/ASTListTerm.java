package de.uka.iti.pseudo.parser.term;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTListTerm extends ASTTerm {

    public ASTListTerm(List<ASTTerm> list) {
        super(list);
        assert list.size() >= 1;
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    @Override
	protected Token getLocationToken() {
    	return getSubterms().get(0).getLocationToken();
	}

}
