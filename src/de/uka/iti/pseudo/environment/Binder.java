package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;

public class Binder {
    
    private String name;

    private TypeReference resultType;

    private TypeReference argumentTypes[];

    private ASTBinderDeclaration declaration;

    private TypeReference varType;

    public Binder(String name, TypeReference resultType, TypeReference varTy,
            TypeReference[] argumentTypes, ASTBinderDeclaration declaration) {
        this.name = name;
        this.resultType = resultType;
        this.argumentTypes = argumentTypes;
        this.declaration = declaration;
        this.varType = varTy;
    }

    public String toString() {
        String ret = "Binder[" + name + ";ret: " + resultType + 
            ";var: " + varType + ":args:";
        for (TypeReference tr : argumentTypes) {
            ret += " " + tr;
        }
        return ret + "]";
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

    public ASTBinderDeclaration getDeclaration() {
        return declaration;
    }

    public TypeReference getVarType() {
        return varType;
    }
    
    

}
