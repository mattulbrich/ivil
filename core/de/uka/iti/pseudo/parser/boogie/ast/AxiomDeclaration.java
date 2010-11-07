package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class AxiomDeclaration extends DeclarationBlock {

    private List<Attribute> attributes;
    private Expression axiom;

    public AxiomDeclaration(Token firstToken, List<Attribute> attributes, Expression axiom) {
        super(firstToken);
        this.attributes = attributes;
        this.axiom = axiom;

        addChild(axiom);
        addChildren(attributes);
    }

    public Expression getAxiom() {
        return axiom;
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
