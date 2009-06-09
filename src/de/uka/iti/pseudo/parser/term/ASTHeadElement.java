package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTHeadElement extends ASTElement {
    
    private ASTElement wrappedElement;

    public ASTHeadElement(ASTElement element) {
        this.wrappedElement = element;
        addChild(element);
    }

    @Override protected Token getLocationToken() {
        throw new UnsupportedOperationException();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        throw new UnsupportedOperationException();
    }

    public ASTElement getWrappedElement() {
        return wrappedElement;
    }

}
