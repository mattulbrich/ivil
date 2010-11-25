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
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;

/**
 * The Class TypingContext is used when type inference happens.
 * 
 * It wraps an instance of {@link TypeUnification} to which it delegates and
 * allows to create new distinct schema type variables and to make types
 * distinct.
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
     * This visitor replaces every <b>type variable</b> with a fresh <b>schena
     * type variable</b>. Occurences of the same type variable are replaces by
     * the same fresh symbol.
     */
    private class SignatureVisitor extends RebuildingTypeVisitor<Void> {

        /** replacement map */
        private Map<TypeVariable, SchemaType> varMap = 
            new HashMap<TypeVariable, SchemaType>();
        
        @Override
        public @NonNull Type visit(@NonNull TypeVariable typeVariable, Void arg) {
            SchemaType tv = varMap.get(typeVariable);
            if(tv == null) {
                tv = newSchemaType();
                varMap.put(typeVariable, tv);
            }
            return tv;
        }
        
        @Override
        public Type visit(SchemaType schemaTypeVariable, Void arg)
                throws TermException {
            assert false : "We do not expect to encounter schema type vars here!";
            return schemaTypeVariable;
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
     * create a new schema type. Every call to this method results in a distinct
     * schema type. These temporary types have integers as names.
     * 
     * @return a fresh schema type object
     */
    public SchemaType newSchemaType() {
        counter ++;
        return new SchemaType(Integer.toString(counter));
    }

    /**
     * Make new distinct signature clone.
     * 
     * Given the signature (result and argument types) use
     * {@link SignatureVisitor} to replace all type variables by fresh schema type
     * variables.
     * 
     * If you have one function
     * <code>'a f('b,'a)<code> and constants <code>'a a</code> and <code>'b b</code>, then 
     * successive calls to makeNewSignature create the new variants:
     * <pre>
     *  %'1 f(%'2,%'1)
     *  %'3 a
     *  %'4 b
     * </pre>
     * and <code>f(a,b)</code> can be unified, as well as <code>f(b,a)</code>.
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
            SignatureVisitor sv = new SignatureVisitor();
            retval[0] = resultType.accept(sv, null);
            for (int i = 0; i < argumentTypes.length; i++) {
                retval[i+1] = argumentTypes[i].accept(sv, null); 
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
     * Given the signature (result and argument types) use
     * {@link SignatureVisitor} to replace all type variables by fresh schema type
     * variables.
     * 
     * If you have one binder
     * {@code 'a (\b 'a; 'b)} and a variable <code>'a a</code> and a constant
     * <code>'b b</code>, then successive calls to makeNewSignature
     * create the new variants:
     * 
     * <pre>
     *  %'1 (\b %'2; %'1)
     *  %'3 a
     *  %'4 b
     * </pre>
     * and <code>(\b a; b)</code> can be unified.
     * 
     * <p>The result is presented in one array, in which the first element is the result of
     * the translation of resultType, the second the type of the quantified variable
     * and the remainder of argumentTypes. 
     * 
     * @param resultType the result type of an expression
     * @param varType the type of the quantified variable
     * @param argumentTypes the argument types
     * 
     * @return an array containing variants of result type, variable type and argument types.
     * Make new distinct signature clone.
     */
    public Type[] makeNewSignature(Type resultType, Type varType, Type[] argumentTypes) {
        try {
            Type[] retval = new Type[argumentTypes.length + 2];
            SignatureVisitor sv = new SignatureVisitor();
            retval[0] = resultType.accept(sv, null);
            retval[1] = varType.accept(sv, null);
            for (int i = 0; i < argumentTypes.length; i++) {
                retval[i+2] = argumentTypes[i].accept(sv, null);
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
    protected void addMapping(SchemaType tv, Type type) throws UnificationException {
        
        if (type instanceof SchemaType) {
            SchemaType tv2 = (SchemaType) type;
            if(isTemporaryVariable(tv2)) {
                super.addMapping(tv2, tv);
                return;
            }
        }
        super.addMapping(tv, type);
    }

    // TODO revise this test now with the new scheme
    private boolean isTemporaryVariable(SchemaType tv2) {
        char c = tv2.getVariableName().charAt(0);
        return c >= '0' && c <= '9';
    }
    
}
