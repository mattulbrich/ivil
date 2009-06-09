package de.uka.iti.pseudo.term.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;

public class TypingContext {
    
    private static class Constraint {

        Type formal;
        Type actual;
        ASTLocatedElement location;
        
        public Constraint(Type formal, Type actual, ASTLocatedElement location) {
            super();
            this.formal = formal;
            this.actual = actual;
            this.location = location;
        }
    }
    
    private class SignatureVisitor extends DefaultTypeVisitor {
        private Map<TypeVariable, TypeVariable> varMap = 
            new HashMap<TypeVariable, TypeVariable>();
        
        @Override
        public Type visit(TypeVariable typeVariable) {
            TypeVariable tv = varMap.get(typeVariable);
            if(tv == null) {
                tv = newTypeVariable();
                varMap.put(typeVariable, tv);
            }
            return tv;
        }
    }

    private TypeVisitor instantiater = new DefaultTypeVisitor() {
        public Type visit(TypeVariable typeVariable) {
            return null;
            // TODO return typeVariableMap.get( resp. typeVariable itself
        };
    };
    
    private List<Constraint> constraints = new ArrayList<Constraint>();
    
    private int counter = 0;

    public Type instantiate(Type rawType) {
        return rawType.visit(instantiater);
    }

    public void addConstraint(Type formal, Type actual, ASTLocatedElement location) {
        constraints.add(new Constraint(formal, actual, location));
    }

    public TypeVariable newTypeVariable() {
        counter ++;
        return new TypeVariable("v" + counter);
    }

    public Type[] makeNewSignature(Type resultType, Type[] argumentTypes) {
        Type[] retval = new Type[argumentTypes.length + 1];
        TypeVisitor sv = new SignatureVisitor();
        retval[0] = resultType.visit(sv);
        for (int i = 0; i < argumentTypes.length; i++) {
            retval[i+1] = argumentTypes[i].visit(sv); 
        }
        return retval;
    }

}
