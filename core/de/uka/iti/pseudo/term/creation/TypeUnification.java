/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.term.creation;

import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.util.AppendMap;

/**
 * The Class TypeUnification is used to unify two types using Robinson's
 * algorithm.
 * 
 * There are two versions of the algorithm used in this class:
 * <ul>
 * <li>leftUnify in which only variables in the left (=first) type may be
 * instantiated. This is used for instance in {@link Application} to check the
 * correctness of a typing. "0 as int" may not be typed as "'a", therefore the
 * asymmetry.
 * <li>unify in which variables in both types can be instantiated. This is for
 * instance used to resolve constraints in
 * {@link TypingContext#solveConstraint(Type, Type)}
 * </ul>
 * 
 * We keep a map from TypeVariables to Types as the recorded substitution. This
 * map is updated when unifying pairs of types
 * 
 */

public class TypeUnification implements Cloneable {

    /**
     * This mapping records the substitution.
     * We use {@link AppendMap} to store the instantiation to be able to
     * efficiently clone mappings.  
     */
    private AppendMap<String, Type> instantiation;

    /**
     * A visitor that replaces all type variables with variants. Since
     * identifiers are not allowed to start with #, they are unique.
     */
    private final static TypeVisitor VARIANT_VISITOR = new DefaultTypeVisitor() {
        public Type visit(TypeVariable typeVariable) {
            return new TypeVariable(TypeVariable.VARIANT_PREFIX + typeVariable.getVariableName());
        };
    };

    /**
     * This visitor is used to actually apply the substitution.
     */
    private TypeVisitor instantiater = new DefaultTypeVisitor() {
        public Type visit(TypeVariable typeVariable) {
            Type replace = instantiation.get(typeVariable.getVariableName());
            if (replace != null)
                return replace;
            else
                return typeVariable;
        };
    };
    
    /**
     * create a new type unification with an empty mapping from 
     * type variables to types.
     */
    public TypeUnification() {
        instantiation = new AppendMap<String, Type>();
    }

    /**
     * create a new type unification with the given initial mapping.
     * 
     * It is not checked if the mapping is valid (for instance acyclic)
     * 
     * @param mapping from type variable names to types
     */
    @Deprecated public TypeUnification(AppendMap<String, Type> map) {
        instantiation = map.clone();
    }
    
    /**
     * create a copy of this type unification object which has a copy of
     * the type variable mapping.
     * 
     * @return a clone of this.
     */
    public TypeUnification clone() {
        try {
            TypeUnification retval = (TypeUnification) super.clone();
            retval.instantiation = instantiation.clone();
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    /**
     * Make variant of a type.
     * 
     * Every type variable <code>'a</code> is replaced by <code>'#a</code>. The same type variable
     * is mapped to the same variant while no new type variable does not appear
     * in a type which is not a variant itself.
     * 
     * @param type
     *            type to make variant of
     * 
     * @return type in which type variables are modified.
     */
    public static @NonNull Type makeVariant(@NonNull Type type) {
        try {
            return type.visit(VARIANT_VISITOR);
        } catch (TermException e) {
            // never thrown in this code
            throw new Error(e);
        }
    }

    /**
     * Use the substitution that has been gathered during unification and apply
     * it to a type.
     * 
     * @param type
     *            some type
     * 
     * @return the result of the application of the substitution to the type
     */
    public @NonNull Type instantiate(@NonNull Type type) {
        try {
            return type.visit(instantiater);
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }
    }

    /**
     * Do unification in which only type variables in the first type argument
     * are matched. A type variable in the second argument does not match a
     * non-variable in the first argument.
     * 
     * Is often combined with one argument changed using
     * {@link #makeVariant(Type)}.
     * 
     * All instantiations are recorded in {@link #instantiation} The results on
     * the mapping are atomic. On success all instantiations appear, otherwise
     * none does.
     * 
     * @param adaptingType
     *            the type that may match variables
     * @param fixType
     *            the fixed type that must not match variables.
     * 
     * @return a type equal to fixType
     * 
     * @throws UnificationException
     *             if the unification fails.
     */
    public @NonNull Type leftUnify(@NonNull Type adaptingType,
            @NonNull Type fixType) throws UnificationException {

        AppendMap<String, Type> copy = instantiation.clone();

        try {
            leftUnify0(adaptingType, fixType);
            assert instantiate(adaptingType).equals(fixType);
            return fixType;
        } catch (UnificationException e) {
            // restore old mapping
            e.addDetail("Cannot left-unify '" + adaptingType + "' and '"
                    + fixType + "'");
            instantiation = copy;
            throw e;
        }

    }

    /*
     * do the actual unification after Robinson
     */
    private void leftUnify0(Type adaptingType, Type fixType)
            throws UnificationException {

        if (adaptingType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) adaptingType;
            if (instantiation.containsKey(tv.getVariableName()))
                adaptingType = instantiate(tv);
        }

        if (adaptingType.equals(fixType))
            return;

        if (adaptingType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) adaptingType;
            if (occursIn(tv, fixType))
                throw new UnificationException("Cannot unify (occur check)",
                        tv, fixType);
            if(isImmutableVariant(tv))
                throw new UnificationException("I cannot instantiate an immutable typevariable");
            addMapping(tv, fixType);
            return;
        }

        assert adaptingType instanceof TypeApplication;

        if (!(fixType instanceof TypeApplication)) {
            throw new UnificationException(
                    "Cannot instantiate type variable on the right",
                    adaptingType, fixType);
        }

        assert fixType instanceof TypeApplication;

        TypeApplication adaptApp = (TypeApplication) adaptingType;
        TypeApplication fixApp = (TypeApplication) fixType;

        if (adaptApp.getSort() != fixApp.getSort()) {
            throw new UnificationException("Incompatible sorts", adaptApp,
                    fixApp);
        }

        Type[] adaptArguments = adaptApp.getArguments();
        Type[] fixArguments = fixApp.getArguments();

        for (int i = 0; i < fixArguments.length; i++) {
            // possibly wrap in try/catch to add detail information
            leftUnify0(adaptArguments[i], fixArguments[i]);
        }
    }

    /*
     * helper function to determine whether a type variable may be instantiated
     * when type checking the application of terms to a function symbol
     * some type variables may not be instantiated.
     * 
     * Example:
     *   (arb as 'a)  and  (2 as int) 
     * may not be presented to $eq('a,'a).
     * 
     * Type variants are made so that the check is
     *   (arb as '#a) and (2 as int)
     *   
     * which cannot be instantiated if '#a may not be instantiated.
     */
    private boolean isImmutableVariant(TypeVariable tv) {
        return tv.getVariableName().startsWith(TypeVariable.VARIANT_PREFIX);
    }

    /**
     * Do unification, matching of two types. A type variable in matches any
     * type. Type applications have to coincide in the sort and must match in
     * all arguments.
     * 
     * All instantiations are recorded in {@link #instantiation} The results on
     * the mapping are atomic. On success all instantiations appear, otherwise
     * none does.
     * 
     * The result is equal to the instantiation of either argument type
     * 
     * @param type1
     *            one type
     * @param type2
     *            another type
     * 
     * @return a type equal to the instantiation applied to type1 (also type2)
     * 
     * @throws UnificationException
     *             if the unification fails.
     */
    public @NonNull Type unify(@NonNull Type type1, @NonNull Type type2)
            throws UnificationException {
        AppendMap<String, Type> copy = instantiation.clone();

        try {
            unify0(type1, type2);
            assert instantiate(type1).equals(instantiate(type2)) : type1 + " vs " + type2;
            return instantiate(type1);
        } catch (UnificationException e) {
            // restore old mapping
            e.addDetail("Cannot unify '" + type1 + "' and '"
                            + type2 + "'");
            instantiation = copy;
            throw e;
        }
    }
    
    /*
     * do the actual unification after Robinson
     */
    private void unify0(Type type1, Type type2) throws UnificationException {

        if (type1 instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type1;
            if (instantiation.containsKey(tv.getVariableName()))
                type1 = instantiate(tv);
        }

        if (type2 instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type2;
            if (instantiation.containsKey(tv.getVariableName()))
                type2 = instantiate(tv);
        }

        if (type1.equals(type2))
            return;

        if (type1 instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type1;
            if (occursIn(tv, type2))
                throw new UnificationException("Cannot unify (occur check)",
                        type1, type2);
            addMapping(tv, type2);
            return;
        }

        if (type2 instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type2;
            if (occursIn(tv, type1))
                throw new UnificationException("Cannot unify (occur check)",
                        type1, type2);
            addMapping(tv, type1);
            return;
        }

        assert type1 instanceof TypeApplication;
        assert type2 instanceof TypeApplication;

        TypeApplication app1 = (TypeApplication) type1;
        TypeApplication app2 = (TypeApplication) type2;

        if (app1.getSort() != app2.getSort()) {
            throw new UnificationException("Incompatible sorts", app1, app2);
        }

        Type[] args1 = app1.getArguments();
        Type[] args2 = app2.getArguments();

        for (int i = 0; i < args1.length; i++) {
            // possibly wrap in try/catch to add detail information
            unify0(args1[i], args2[i]);
        }

    }

    /*
     * Adds a mapping.
     * 
     * We have to update all existing mappings afterwards. Since no loops (occur
     * check!) are allowed, we apply it to everything (including tv).
     * 
     * @param tv the tv
     * 
     * @param type the type
     */
    private void addMapping(TypeVariable tv, Type type) {

        assert instantiation.get(tv.getVariableName()) == null;
        assert !occursIn(tv, type);

        instantiation.put(tv.getVariableName(), type);
        
        // check whether this is needed or not: it is :)
        for (String t : instantiation.keySet()) {
            instantiation.put(t, instantiate(instantiation.get(t)));
        }

    }

    /*
     * Occur check. Does tv appear in type?
     */
    private boolean occursIn(final TypeVariable tv, Type type) {
        TypeVisitor vis = new DefaultTypeVisitor() {
            @Override public Type visit(TypeVariable typeVariable)
                    throws TermException {
                if (typeVariable.equals(tv))
                    throw new TermException("TypeVariable found!");
                return typeVariable;
            }
        };

        try {
            type.visit(vis);
            // no exception: not found
            return false;
        } catch (TermException e) {
            // exception: type variable has been found
            return true;
        }
    }

    /**
     * get the instantiation map which maps type variables to types.
     * 
     * @return a mapping from type variables to maps.
     */
    public Map<String, Type> getInstantiation() {
        // possibly wrap in Collections.unmodifiable?
        return instantiation;
    }

}
