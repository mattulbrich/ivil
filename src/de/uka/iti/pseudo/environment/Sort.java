package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;

public class Sort {

    private String name;

    private int arity;

    private ASTSortDeclaration declaration;

    public Sort(String name, int arity, ASTSortDeclaration declaration) {
        super();
        this.name = name;
        this.arity = arity;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public int getArity() {
        return arity;
    }

    public ASTSortDeclaration getDeclaration() {
        return declaration;
    }
    
    public String toString() {
        return "Sort[" + name + ";" + arity + "]";
    }

}
