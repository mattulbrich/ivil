package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.term.Type;

public class Function {

    private String name;

    private Type resultType;

    private Type argumentTypes[];
    
    private ASTFunctionDeclaration declaration;

    public Function(String name, Type resultType,
            Type[] argumentTypes, ASTFunctionDeclaration declaration) {
        super();
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public Type getResultType() {
        return resultType;
    }

    public Type[] getArgumentTypes() {
        return argumentTypes;
    }

    public ASTFunctionDeclaration getDeclaration() {
        return declaration;
    }
    
    public String toString() {
        String ret = "Function[" + name + ";ret: " + resultType +";args:";
        for (Type tr : argumentTypes) {
            ret += " " + tr;
        }
        return ret + "]";
    }

}
