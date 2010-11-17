package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

final public class UserTypeDefinition extends ASTElement {
    
    private final Token name;
    // this is only needed to process type table construction, as the argnames
    // can specify
    private final List<String> argnames;
    private final Type definition;

    public UserTypeDefinition(Token name, List<String> argnames, Type parent) {
        this.name = name;
        this.argnames = argnames;
        this.definition = parent;

        if (null != definition)
            addChild(definition);
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

    public List<String> getArgnames() {
        return argnames;
    }

    public Type getDefinition() {
        return definition;
    }

}
