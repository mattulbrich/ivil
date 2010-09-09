/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
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
 * @author mattias ulbrich
 * 
 * @see TypingResolver
 * @see TypeUnification
 */
public class TypingContext extends TypeUnification {
    
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
     * Solve a constraint.
     * 
     * <p>
     * This is a try to unify two type expressions.
     * 
     * <p>
     * <b>Note:</b> This implementation is identical to
     * {@link #unify(Type, Type)}
     * 
     * @param formal
     *            the formal type
     * @param actual
     *            the actual type
     * 
     * @throws UnificationException
     *             if the unification fails.
     */
    public void solveConstraint(Type formal, Type actual) throws UnificationException {
    	
    	unify(formal, actual);
    	
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
     * makeNewSignature creates the new variants:
     * <pre>
     *  '1 f('2,'1)
     *  '3 a
     *  '4 b
     * </pre>
     * and <code>f(a,b)</code> can be unified.
     * 
     * <p>The result is presented in one array, in which the first element is the result of
     * the translation of resultType and the remainder of argumentTypes. 
     * 
     * @param resultType the result type of an expression
     * @param argumentTypes the argument types
     * 
     * @return an array containg variants of resulttype and argument types.
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

    /**
     * {@inheritDoc}
     * 
     * In typing resolution, if two type variables are to be compared, we prefer
     * that the temporary variable be instantiated rather than the usual one.
     */
    @Override
    protected void addMapping(TypeVariable tv, Type type) throws UnificationException {
        
        if (type instanceof TypeVariable) {
            TypeVariable tv2 = (TypeVariable) type;
            if(isTemporaryVariable(tv2)) {
                super.addMapping(tv2, tv);
                return;
            }
        }
        super.addMapping(tv, type);
    }

    private boolean isTemporaryVariable(TypeVariable tv) {
        char c = tv.getVariableName().charAt(0);
        return c >= '0' && c <= '9';
    }
    
}
