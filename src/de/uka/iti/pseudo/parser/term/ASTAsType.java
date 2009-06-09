package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTAsType extends ASTTerm {

    private ASTTypeRef asType;

    public ASTAsType(ASTTerm term, ASTTypeRef type) {
        super(Collections.<ASTTerm>singletonList(term));
        addChild(type);
        this.asType = type;
    }

    @Override
    protected Token getLocationToken() {
        return getSubterms().get(0).getLocationToken();
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTTypeRef getAsType() {
        return asType;
    }
    
    public ASTTerm getTerm() {
        return getSubterms().get(0);
    }

}
