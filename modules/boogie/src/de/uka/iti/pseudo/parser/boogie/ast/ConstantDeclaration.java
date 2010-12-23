package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class ConstantDeclaration extends DeclarationBlock {

    private final boolean unique;
    private final List<Variable> names;
    private final OrderSpecification spec;

    public ConstantDeclaration(Token firstToken, List<Attribute> attributes, boolean unique, List<Variable> names,
            OrderSpecification spec) {
        super(firstToken, attributes);
        this.unique = unique;
        this.names = names;
        this.spec = spec;

        addChildren(names);
        if (spec != null)
            addChild(spec);
    }

    public boolean isUnique() {
        return unique;
    }

    public List<Variable> getNames() {
        return Collections.unmodifiableList(names);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean hasOrderSpecification() {
        return null != spec;
    }

    public OrderSpecification getOrderSpecification() {
        assert null != spec : "you missed a check";
        return spec;
    }

}
