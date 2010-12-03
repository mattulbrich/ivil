package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public class FunctionDeclaration extends DeclarationBlock implements NamedASTElement {

    private final String name;

    private final List<String> typeParameters;
    private final List<Variable> inParameters;
    private final Variable outParemeter;

    // ! @note: can be null if a function has no specification
    private final Expression expression;

    public FunctionDeclaration(Token firstToken, List<Attribute> attributes, Token name, List<Token> typeParameters,
            List<Variable> inParam, Variable outParam, Expression expression) {
        super(firstToken, attributes);
        this.name = name.image;

        this.typeParameters = ASTConversions.toStringList(typeParameters);
        this.inParameters = inParam;
        this.outParemeter = outParam;

        this.expression = expression;

        addChildren(inParam);
        addChild(outParam);

        if (null != expression)
            addChild(expression);
    }


    public String getName() {
        return name;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public String toString(){
        return "FunctionDeclaration [" + name + ", " + getLocation() + "]";
    }


    public Variable getOutParemeter() {
        return outParemeter;
    }


    public List<Variable> getInParameters() {
        return inParameters;
    }


    public List<String> getTypeParameters() {
        return typeParameters;
    }
}
