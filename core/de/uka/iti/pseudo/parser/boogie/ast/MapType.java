package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class MapType extends Type {

    private final Token location;
    private final List<Token> params;
    private final List<Type> domain;
    private final Type range;

    public MapType(Token first, List<Token> params, List<Type> domain, Type range) {
        location = first;
        this.params = params;
        this.domain = domain;
        this.range = range;
    }

    @Override
    public String getPrettyName() {
        // TODO implement pretty names
        return "some map type";
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<Token> getParams() {
        return params;
    }

    public List<Type> getDomain() {
        return domain;
    }

    public Type getRange() {
        return range;
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

}
