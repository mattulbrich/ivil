package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * This struct contains data used in OrderSpecification.
 * 
 * @author timm.felden@felden.com
 */
public final class OrderSpecParent extends de.uka.iti.pseudo.parser.boogie.ASTElement {

    public final boolean unique;
    public final Token name;

    public OrderSpecParent(boolean unique, Token name) {
        this.unique = unique;
        this.name = name;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return name;
    }

    public String getName() {
        return name.image;
    }

}
