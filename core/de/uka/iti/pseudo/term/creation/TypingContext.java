/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.HashMap;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;

/**
 * The Class TypingContext is used when type inference happens.
 * 
 * It wraps an instance of {@link TypeUnification} to which it delegates and
 * allows to create new distinct type variables and to make types distinct.
 * 
 * Type inference is done by solving "constraints" via
 * {@link #solveConstraint(Type, Type)} which are bidirectional unification
 * problems.
 * 
 * @see TypingResolver
 * @see TypeUnification
 */
public class TypingContext {
    
	/**
     * This visitor replaces every type variable with a fresh type variables.
     * Occurences of the same type variable are replaces by the same fresh
     * symbol.
     */
    private class SignatureVisitor extends DefaultTypeVisitor {

        /** replacement map */
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
    
    /**
     * This counter is used to create distinct type variables.
     */
    private int counter = 0;
    
    /**
     * The unificator used in the background
     */
    private TypeUnification unify = new TypeUnification();

    /**
     * Use the substitution that has been gathered during constraint solving
     * and apply it to a type.
     * 
     * @param type
     *            some type
     * 
     * @return the result of the application of the substitution to the type
     */
    public Type instantiate(Type type) {
    	return unify.instantiate(type);
    }

    /**
     * Solve a constraint.
     * 
     * This is infact a try to unify two type expressions.
     * 
     * @param formal the formal type
     * @param actual the actual type
     * 
     * @throws UnificationException if the unification fails.
     */
    public void solveConstraint(Type formal, Type actual) throws UnificationException {
    	
    	unify.unify(formal, actual);
    	
    }

    /**
     * create a new type variable. Every call to this method results in a
     * distinct type variable. These temporary type variables have integers as
     * names.
     * 
     * @return a fresh type variable object
     */
    public TypeVariable newTypeVariable() {
        counter ++;
        return new TypeVariable(Integer.toString(counter));
    }

    /**
     * Make new distinct signature clone.
     * 
     * Given the signature (result and argument types) use
     * {@link SignatureVisitor} to replace all type variables by fresh type
     * variables.
     * 
     * If you have one function
     * <code>'a f('b,'a)<code> and constants <code>'a a</code> and <code>'b b</code>, then 
     * 
     * TODO: Auto-generated Javadoc not finished
     * 
     * @param resultType the result type
     * @param argumentTypes the argument types
     * 
     * @return the type[]
     */
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
    
    /**
     * Make new distinct signature clone.
     *
     * TODO: Auto-generated Javadoc not finished
     * 
     */
    public Type[] makeNewSignature(Type resultType, Type varType, Type[] argumentTypes) {
        try {
            Type[] retval = new Type[argumentTypes.length + 2];
            TypeVisitor sv = new SignatureVisitor();
            retval[0] = resultType.visit(sv);
            retval[1] = varType.visit(sv);
            for (int i = 0; i < argumentTypes.length; i++) {
                retval[i+2] = argumentTypes[i].visit(sv); 
            }
            return retval;
        } catch (TermException e) {
            // never thrown in this code
            throw new Error(e);
        }
    }

}
