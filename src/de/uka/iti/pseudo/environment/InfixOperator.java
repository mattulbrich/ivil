package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;

public class InfixOperator {

    private String name;

    private String infixOpName;

    private int precedence;

    private ASTFunctionDeclaration declaration;

    public InfixOperator(String name, String infixOpName, int precedence,
            ASTFunctionDeclaration declaration) {
        super();
        this.name = name;
        this.infixOpName = infixOpName;
        this.precedence = precedence;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public String getInfixOpName() {
        return infixOpName;
    }

    public int getPrecedence() {
        return precedence;
    }

    public ASTFunctionDeclaration getDeclaration() {
        return declaration;
    }
    
    @Override
    public String toString() {
        return "InfixOperator[infix: " +infixOpName + ";prefix: " + name + ";precedence: " +precedence +"]"; 
    }

}
