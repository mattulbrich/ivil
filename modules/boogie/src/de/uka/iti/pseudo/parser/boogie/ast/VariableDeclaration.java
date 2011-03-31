package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTType;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

/**
 * This element is used to represent a variable or constant declaration with
 * type annotation.
 * 
 * @author timm.felden@felden.com
 */
final public class VariableDeclaration extends ASTElement {

    private final Token location;
    private final String name;
    private final ASTType type;
    private final boolean constant;
    private final boolean unique;
    private final boolean isQuantified;
    private final Expression where;

    public VariableDeclaration(Token name, ASTType type, boolean constant, boolean unique, boolean isQuantified,
            Expression where) {
        this.name = ASTConversions.getEscapedName(name);
        this.location = name;
        this.type = type;
        this.constant = constant;
        this.unique = unique;
        this.isQuantified = isQuantified;

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
     * @param unique
     * @param isQuantified
     * @param where
     */
    public VariableDeclaration(String name, ASTType type, boolean constant, boolean unique, boolean isQuantified,
            Expression where) {
        this.name = name;
        this.location = type.getLocationToken();
        this.type = type;
        this.constant = constant;
        this.unique = unique;
        this.isQuantified = isQuantified;

        // any variable declaration has a where clause which defaults to true
        if (null != where)
            this.where = where;
        else
            this.where = new TrueExpression(location);

        addChild(type);
        addChild(this.where);
    }

    public ASTType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isConstant() {
        return constant;
    }

    public boolean isUnique() {
        return unique;
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

    public boolean isQuantified() {
        return isQuantified;
    }

}
