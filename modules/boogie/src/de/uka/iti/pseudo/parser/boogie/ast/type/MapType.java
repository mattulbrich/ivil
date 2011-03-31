package de.uka.iti.pseudo.parser.boogie.ast.type;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.NamedASTElement;

public final class MapType extends ASTType implements NamedASTElement {

    private final Token location;
    private final List<ASTTypeParameter> params;
    private final List<ASTType> domain;
    private final ASTType range;

    public MapType(Token first, List<Token> params, List<ASTType> domain, ASTType range) {
        location = first;
        this.domain = domain;
        this.range = range;

        this.params = new ArrayList<ASTTypeParameter>(params.size());
        for (Token name : params)
            this.params.add(new ASTTypeParameter(name));

        addChildren(this.params);
        addChildren(domain);
        addChild(range);
    }

    @Override
    public String getPrettyName() {
        return "map types need implementation of pretty printed names";
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<ASTTypeParameter> getTypeParameters() {
        return params;
    }

    public List<ASTType> getDomain() {
        return domain;
    }

    public ASTType getRange() {
        return range;
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    @Override
    public String getName() {
        return "map_" + getLocation().replace(":", "_");
    }

}
