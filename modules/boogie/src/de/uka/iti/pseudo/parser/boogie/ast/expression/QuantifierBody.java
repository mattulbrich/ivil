package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.NamedASTElement;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTTypeParameter;

public final class QuantifierBody extends ASTElement implements NamedASTElement {

    private final Token location;
    private final List<Attribute> attributes;
    private final List<Trigger> triggers;
    private final List<ASTTypeParameter> typeParameters;
    private final List<VariableDeclaration> quantifiedVariableDeclarations;
    private final Expression body;

    public QuantifierBody(Token location, List<Attribute> attributes, List<Trigger> triggers, List<Token> typeArgs,
            List<VariableDeclaration> vars,
            Expression body) {
        this.location = location;
        this.attributes = attributes;
        this.triggers = triggers;
        this.quantifiedVariableDeclarations = vars;
        this.body = body;

        this.typeParameters = new ArrayList<ASTTypeParameter>(typeArgs.size());

        addChildren(this.typeParameters);
        addChildren(vars);
        addChild(body);
        addChildren(attributes);
        addChildren(triggers);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    public List<ASTTypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public List<VariableDeclaration> getQuantifiedVariables() {
        return quantifiedVariableDeclarations;
    }

    public Expression getBody() {
        return body;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return "body_at" + getLocation().replace(":", "_");
    }

}
