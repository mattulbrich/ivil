package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class ConstantDeclaration extends DeclarationBlock {

    private final List<Attribute> attributes;
    private final boolean unique;
    private final List<Variable> names;

    public ConstantDeclaration(Token firstToken, List<Attribute> attributes, boolean unique, List<Variable> names) {
        super(firstToken);
        this.attributes = attributes;
        this.unique = unique;
        this.names = names;

        addChildren(names);
        addChildren(attributes);
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
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

}
