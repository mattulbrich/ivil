package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;

public class Function {

    private String name;

    private TypeReference resultType;

    private TypeReference argumentTypes[];
    
    private ASTFunctionDeclaration declaration;

    public Function(String name, TypeReference resultType,
            TypeReference[] argumentTypes, ASTFunctionDeclaration declaration) {
        super();
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public TypeReference getResultType() {
        return resultType;
    }

    public TypeReference[] getArgumentTypes() {
        return argumentTypes;
    }

    public ASTFunctionDeclaration getDeclaration() {
        return declaration;
    }
    
    public String toString() {
        String ret = "Function[" + name + ";ret: " + resultType +";args:";
        for (TypeReference tr : argumentTypes) {
            ret += " " + tr;
        }
        return ret + "]";
    }

}
