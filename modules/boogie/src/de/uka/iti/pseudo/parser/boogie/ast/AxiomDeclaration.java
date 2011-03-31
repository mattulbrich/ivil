package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;

public class AxiomDeclaration extends DeclarationBlock {

    private final Expression axiom;

    public AxiomDeclaration(Token firstToken, List<Attribute> attributes, Expression axiom) {
        super(firstToken, attributes);
        this.axiom = axiom;

        addChild(axiom);
    }

    public Expression getAxiom() {
        return axiom;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
