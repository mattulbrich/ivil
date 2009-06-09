package de.uka.iti.pseudo.environment;

import java.util.Collection;

public class TypeApplication extends TypeReference {
    
    private TypeReference[] typeParamters;
    private Sort sort;
    
    public TypeApplication(Sort sort, TypeReference[] typeParameters) {
        this.sort = sort;
        this.typeParamters = typeParameters;
    }
    
    @Override
    public void collectTypeVariables(Collection<String> coll) {
        for (TypeReference tr : typeParamters) {
            tr.collectTypeVariables(coll);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("!").append(sort.getName());
        for (int i = 0; i < typeParamters.length; i++) {
            sb.append(i == 0 ? "(" : ",");
            sb.append(typeParamters[i]);
        }
        if(typeParamters.length > 0)
            sb.append(")");
        
        return sb.toString();
    }
}
