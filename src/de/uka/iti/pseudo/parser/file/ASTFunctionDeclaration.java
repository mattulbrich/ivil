package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTFunctionDeclaration extends ASTFileElement {

    private ASTType rangeType;

    private List<ASTType> argumentTypes;

    private Token precedence;

    private Token infixOperator;

    private Token name;

    public ASTFunctionDeclaration(ASTType range, Token name,
            List<ASTType> tyrefs) {
        this.rangeType = range;
        this.argumentTypes = tyrefs;
        this.name = name;
        addChild(range);
        addChildren(tyrefs);
    }

    public ASTFunctionDeclaration(ASTType range, Token name,
            List<ASTType> tyrefs, Token infixOperator, Token precedence) {
        this(range, name, tyrefs);
        this.infixOperator = infixOperator;
        this.precedence = precedence;
    }

    public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTType getRangeType() {
        return rangeType;
    }

    public List<ASTType> getArgumentTypes() {
        return Collections.unmodifiableList(argumentTypes);
    }

    public Token getPrecedence() {
        return precedence;
    }

    public Token getInfixOperator() {
        return infixOperator;
    }

    public Token getName() {
        return name;
    }

    public boolean isInfix() {
        return infixOperator != null;
    }

    public Token getLocationToken() {
        return name;
    }

}
