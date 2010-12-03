package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class QuantifierBody extends ASTElement implements NamedASTElement {

    private final Token location;
    private final List<Attribute> attributes;
    private final List<Trigger> triggers;
    private final List<String> typeParameters;
    private final List<Variable> quantifiedVariables;
    private final Expression body;

    public QuantifierBody(Token location, List<Attribute> attributes, List<Trigger> triggers, List<Token> typeArgs,
            List<Variable> vars,
            Expression body) {
        this.location = location;
        this.attributes = attributes;
        this.triggers = triggers;
        this.typeParameters = ASTConversions.toStringList(typeArgs);
        this.quantifiedVariables = vars;
        this.body = body;

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

    public List<String> getTypeParameters() {
        return typeParameters;
    }

    public List<Variable> getQuantifiedVariables() {
        return quantifiedVariables;
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
        return "body_at_" + getLocation().replace(":", "_");
    }

}
