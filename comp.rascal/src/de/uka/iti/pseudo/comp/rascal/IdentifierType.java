package de.uka.iti.pseudo.comp.rascal;

public class IdentifierType implements Type {

    private String name;

    public IdentifierType(String name) {
        super();
        this.name = name;
    }

    @Override 
    public String toString() {
        return name;
    }

    @Override public String toSimpleType() {
        if("int".equals(name))
            return "int";
        
        if("bool".equals(name))
            return "bool";
        
        return "ref";
    }
    
}
