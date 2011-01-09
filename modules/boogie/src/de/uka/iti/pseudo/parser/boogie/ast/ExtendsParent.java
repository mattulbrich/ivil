package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * Some constant is has name as immediate parent.
 * 
 * @author timm.felden@felden.com
 */
public final class ExtendsParent extends ASTElement {

    public final boolean unique;
    public final Token name;

    public ExtendsParent(boolean unique, Token name) {
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
