package de.uka.iti.pseudo.comp.rascal;

public class ArrayType implements Type {
    
    Type wrappedType;

    public ArrayType(Type wrapped) {
        this.wrappedType = wrapped;
    }
    
    @Override public String toString() {
        return "ARRAY OF " + wrappedType;
    }

    @Override public String toSimpleType() {
        return "ref";
    }

}
