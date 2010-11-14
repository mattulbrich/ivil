package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class QuantifierBody extends ASTElement {

    private final Token location;
    private final List<Attribute> attributes;
    private final List<Trigger> triggers;
    private final List<String> typeArgs;
    private final List<Variable> quantifiedVariables;
    private final Expression body;

    public QuantifierBody(Token location, List<Attribute> attributes, List<Trigger> triggers, List<Token> typeArgs,
            List<Variable> vars,
            Expression body) {
        this.location = location;
        this.attributes = attributes;
        this.triggers = triggers;
        this.typeArgs = ASTConversions.toStringList(typeArgs);
        this.quantifiedVariables = vars;
        this.body = body;

        addChildren(attributes);
        addChildren(triggers);
        addChildren(vars);
        addChild(body);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    public List<String> getTypeArgs() {
        return typeArgs;
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

}
