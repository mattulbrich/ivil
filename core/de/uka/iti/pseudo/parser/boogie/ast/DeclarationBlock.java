package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.Token;

public abstract class DeclarationBlock extends ASTElement {

    protected final Token firstToken;
    protected final List<Attribute> attributes;

    public DeclarationBlock(Token firstToken, List<Attribute> attributes) {
        super();
        this.firstToken = firstToken;
        this.attributes = attributes;

        addChildren(attributes);
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public Token getLocationToken() {
        return firstToken;
    }
}
