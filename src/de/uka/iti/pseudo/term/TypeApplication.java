package de.uka.iti.pseudo.term;

import java.util.Collection;

import de.uka.iti.pseudo.environment.Sort;

public class TypeApplication extends Type {
    
    private Type[] typeParamters;
    private Sort sort;
    
    public TypeApplication(Sort sort, Type[] typeParameters) {
        this.sort = sort;
        this.typeParamters = typeParameters;
    }
    
    @Override
    public void collectTypeVariables(Collection<String> coll) {
        for (Type tr : typeParamters) {
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
