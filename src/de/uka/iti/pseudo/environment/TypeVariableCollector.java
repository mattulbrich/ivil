package de.uka.iti.pseudo.environment;

import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;

public class TypeVariableCollector implements TypeVisitor {
    
    Set<TypeVariable> typeVariables = new HashSet<TypeVariable>();
    
    private TypeVariableCollector() {}

    @Override 
    public Type visit(TypeVariable typeVariable) throws TermException {
        typeVariables.add(typeVariable);
        return null;
    }

    @Override public Type visit(TypeApplication typeApplication)
            throws TermException {
        for (Type t : typeApplication.getArguments()) {
            t.visit(this);
        }
        return null;
    }

    public static Set<TypeVariable> collect(Type type) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            type.visit(tvc);
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return tvc.typeVariables;
    }
    
    
    

}
