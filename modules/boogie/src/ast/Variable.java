package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * This element is used to represent a variable or constant declaration with
 * type annotation.
 * 
 * @author timm.felden@felden.com
 */
final public class Variable extends ASTElement {

    private final Token location;
    private final String name;
    private final Type type;
    private final boolean constant;
    private final Expression where;

    public Variable(Token name, Type type, boolean constant, Expression where) {
        this.name = name.image;
        this.location = name;
        this.type = type;
        this.constant = constant;

        // any variable declaration has a where clause which defaults to true
        if (null != where)
            this.where = where;
        else
            this.where = new TrueExpression(name);

        addChild(type);
        addChild(this.where);
    }

    /**
     * This constructor is used to create implicit variables in function
     * declarations.
     * 
     * @param name
     * @param type
     * @param constant
     * @param where
     */
    public Variable(String name, Type type, boolean constant, Expression where) {
        this.name = name;
        this.location = type.getLocationToken();
        this.type = type;
        this.constant = constant;

        // any variable declaration has a where clause which defaults to true
        if (null != where)
            this.where = where;
        else
            this.where = new TrueExpression(location);

        addChild(type);
        addChild(this.where);
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isConstant() {
        return constant;
    }

    public Expression getWhereClause() {
        return where;
    }

    @Override
    public Token getLocationToken() {
        return location;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public String toString() {
        return (constant ? "Constant [" : "Variable [") + name + ", " + getLocation() + "]";
    }

}
