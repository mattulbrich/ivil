package de.uka.iti.pseudo.term;

import java.util.Collection;

public class TypeVariable extends Type {

    private String typeVar;

    public TypeVariable(String typeVar) {
        this.typeVar = typeVar;
    }
    
    @Override
    public void collectTypeVariables(Collection<String> coll) {
        coll.add(typeVar);
    }
    
    @Override
    public String toString() {
        return "!" + typeVar; 
    }

}
