package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;

// TODO DOC high prio

public class TypingContext {
    
	/**
	 * This visitor replaces every type variable with a fresh type variables.
	 * Occurences of the same type variable are replaces by the same fresh symbol.
	 */
    private class SignatureVisitor extends DefaultTypeVisitor {
        private Map<TypeVariable, TypeVariable> varMap = 
            new HashMap<TypeVariable, TypeVariable>();
        
        @Override
        public @NonNull Type visit(@NonNull TypeVariable typeVariable) {
            TypeVariable tv = varMap.get(typeVariable);
            if(tv == null) {
                tv = newTypeVariable();
                varMap.put(typeVariable, tv);
            }
            return tv;
        }
    }

    
    
    private int counter = 0;
    private TypeUnification unify = new TypeUnification();

    public Type instantiate(Type type) {
    	return unify.instantiate(type);
    }

    public void solveConstraint(Type formal, Type actual) throws UnificationException {
    	
    	unify.unify(formal, actual);
    	
    }

    public TypeVariable newTypeVariable() {
        counter ++;
        return new TypeVariable(Integer.toString(counter));
    }

    public Type[] makeNewSignature(Type resultType, Type[] argumentTypes) {
        try {
			Type[] retval = new Type[argumentTypes.length + 1];
			TypeVisitor sv = new SignatureVisitor();
			retval[0] = resultType.visit(sv);
			for (int i = 0; i < argumentTypes.length; i++) {
			    retval[i+1] = argumentTypes[i].visit(sv); 
			}
			return retval;
		} catch (TermException e) {
			// never thrown in this code
			throw new Error(e);
		}
    }

}
