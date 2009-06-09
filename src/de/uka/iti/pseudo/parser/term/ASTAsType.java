package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTAsType extends ASTTerm {

    private ASTType asType;

    public ASTAsType(ASTTerm term, ASTType type) {
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

    public ASTType getAsType() {
        return asType;
    }
    
    public ASTTerm getTerm() {
        return getSubterms().get(0);
    }

}
