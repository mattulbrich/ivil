package de.uka.iti.pseudo.environment;

import java.util.Collection;

public class TypeVarTypeReference extends TypeReference {

    private String typeVar;

    public TypeVarTypeReference(String typeVar) {
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
