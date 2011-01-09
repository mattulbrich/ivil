package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class ConstantDeclaration extends DeclarationBlock {

    private final boolean unique;
    private final List<VariableDeclaration> names;
    private final List<ExtendsParent> parents;
    private final boolean complete;

    public ConstantDeclaration(Token firstToken, List<Attribute> attributes, boolean unique,
            List<VariableDeclaration> names,
            List<ExtendsParent> parents, boolean complete) {
        super(firstToken, attributes);
        this.unique = unique;
        this.names = names;
        this.parents = parents;
        this.complete = complete;

        addChildren(names);
        addChildren(parents);
    }

    public boolean isUnique() {
        return unique;
    }

    public List<VariableDeclaration> getNames() {
        return Collections.unmodifiableList(names);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<ExtendsParent> getParents() {
        return parents;
    }

    public boolean isComplete() {
        return complete;
    }

}
