package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;

public class FixOperator {

    private String name;

    private String opIdentifier;

    private int precedence;

    private ASTFunctionDeclaration declaration;

    public FixOperator(String name, String opIdentifier, int precedence,
            ASTFunctionDeclaration declaration) {
        super();
        this.name = name;
        this.opIdentifier = opIdentifier;
        this.precedence = precedence;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public String getOpIdentifier() {
        return opIdentifier;
    }

    public int getPrecedence() {
        return precedence;
    }

    public ASTFunctionDeclaration getDeclaration() {
        return declaration;
    }
    
    @Override
    public String toString() {
        return "FixOperator[op: " +opIdentifier + "; function: " + name + "; precedence: " +precedence +"]"; 
    }

}
