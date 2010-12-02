package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class Attribute extends ASTElement {

    private final Token name;
    private final List<AttributeParameter> params;

    public Attribute(Token name, List<AttributeParameter> params) {
        this.name = name;
        this.params = params;

        addChildren(params);
    }

    @Override
    public Token getLocationToken() {
        return name;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name.image;
    }

    public List<AttributeParameter> getParams() {
        return params;
    }

}
