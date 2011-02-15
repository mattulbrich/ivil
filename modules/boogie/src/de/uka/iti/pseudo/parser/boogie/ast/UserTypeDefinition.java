package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

final public class UserTypeDefinition extends ASTElement implements NamedASTElement {
    
    private final Token location;
    private final String name;
    // this is only needed to process type table construction, as the argnames
    // can specify
    private final List<String> argnames;
    private final Type definition;

    public UserTypeDefinition(Token name, List<String> argnames, Type parent) {
        this.name = ASTConversions.getEscapedName(name);
        this.location = name;
        this.argnames = argnames;
        this.definition = parent;

        if (null != parent)
            addChild(parent);
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public List<String> getTypeParameters() {
        return argnames;
    }

    public Type getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "UserTypeDefinition [" + name + "] @" + location.beginColumn;
    }
}
