package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTHeadElement extends ASTElement {
    
    public ASTHeadElement(ASTElement element) {
        addChild(element);
    }

    @Override protected Token getLocationToken() {
        throw new UnsupportedOperationException();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        throw new UnsupportedOperationException();
    }

    public ASTElement getWrappedElement() {
        return getChildren().get(0);
    }

}
