package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class MapType extends ASTType implements NamedASTElement {

    private final Token location;
    private final List<String> params;
    private final List<ASTType> domain;
    private final ASTType range;

    public MapType(Token first, List<Token> params, List<ASTType> domain, ASTType range) {
        location = first;
        this.params = ASTConversions.toEscapedNameList(params);
        this.domain = domain;
        this.range = range;

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

    public List<String> getTypeParameters() {
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
