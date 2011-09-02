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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaType;
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
 * <li>leftUnify in which only schema variables in the left (=first) type may be
 * instantiated. This is used for instance in {@link Application} to check the
 * correctness of a typing. "0 as int" may not be typed as "%'a", therefore the
 * asymmetry.
 * <li>unify in which variables in both types can be instantiated. This is for
 * instance used to resolve constraints in
 * {@link TypingContext#solveConstraint(Type, Type)}
 * </ul>
 * 
 * <p>
 * We keep a map from {@link SchemaType}s to {@link Type}s as the
 * recorded substitution. This map is updated when unifying pairs of types.
 * 
 */
@SuppressWarnings("nullness")
public class TypeUnification implements Cloneable {

    /**
     * This mapping records the substitution.
     * We use {@link AppendMap} to store the instantiation to be able to
     * efficiently clone mappings.  
     */
    private AppendMap<String, Type> instantiation;

    /**
     * A visitor that replaces all type variables with schema type variables.
     */
    private final static RebuildingTypeVisitor<Void> VARIANT_VISITOR = new RebuildingTypeVisitor<Void>() {
        public Type visit(TypeVariable typeVariable, Void parameter) {
            return SchemaType.getInst(SchemaType.VARIANT_PREFIX + typeVariable.getVariableName());
        };
    };

    /**
     * This visitor is used to actually apply the substitution.
     */
    private TypeVisitor<Type, Void> instantiater = new RebuildingTypeVisitor<Void>() {
        public Type visit(SchemaType schemaTypeVariable, Void parameter) {
            Type replace = instantiateSchemaType(schemaTypeVariable);
            if (replace != null)
                return replace;
            else
                return schemaTypeVariable;
        };
    };
    
    /**
     * create a new type unification with an empty mapping from 
     * type variables to types.
     */
    public TypeUnification() {
        instantiation = new AppendMap<String, Type>();
    }

//    /**
//     * create a new type unification with the given initial mapping.
//     * 
//     * It is not checked if the mapping is valid (for instance acyclic)
//     * 
//     * @param mapping from type variable names to types
//     */
//    @Deprecated public TypeUnification(AppendMap<String, Type> map) {
//        instantiation = map.clone();
//    }
    
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
     * Make a variant of a type that can be instantiated.
     * 
     * Every <b>type variable</b> <code>'a</code> is replaced by an instantiatable schema
     * type <code>%'#a</code>. The same type variable is mapped to the same schema entity.
     * 
     * The extra # is included to make these names unique and have no name clashes with
     * existing types.
     * 
     * @param type
     *            type to make variant of
     * 
     * @return type in which type variables are modified.
     * 
     * @see Application#typeCheck()
     * @see Binding#typeCheck()
     */
    public static @NonNull Type makeSchemaVariant(@NonNull Type type) {
        try {
            return type.accept(VARIANT_VISITOR, null);
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
            return type.accept(instantiater, null);
        } catch (TermException e) {
            // not thrown in this code
            throw new Error(e);
        }
    }

    /**
     * Instantiate a schema type.
     * 
     * <p>
     * In this implementation this is delegated to the {@link #instantiation}
     * map. Derived classes may choose to behave differently but should act
     * accordingly in {@link #addMapping(SchemaType, Type)}.
     * 
     * @param schemaType
     *            the schema type
     * 
     * @return the type stored in the map.
     */
    protected Type instantiateSchemaType(SchemaType schemaType) {
        return instantiation.get(schemaType.getVariableName());
    }
    
    
    /*
     * does the actual LEFT unification after Robinson
     */
    private class Unifier implements TypeVisitor<Void, Type> {
        
        public Unifier() {
        }

        @Override
        public Void visit(TypeApplication adaptApp, Type fixType) throws TermException {
            if (fixType instanceof SchemaType) {
                fixType.accept(this, adaptApp);
            } else
            
            if(fixType instanceof TypeApplication) {
                TypeApplication fixApp = (TypeApplication) fixType;
                
                if (adaptApp.getSort() != fixApp.getSort()) {
                    throw new UnificationException("Incompatible sorts",
                            adaptApp, fixApp);
                }

                List<Type> adaptArguments = adaptApp.getArguments();
                List<Type> fixArguments = fixApp.getArguments();

                for (int i = 0; i < fixArguments.size(); i++) {
                    // possibly wrap in try/catch to add detail information
                    adaptArguments.get(i).accept(this, fixArguments.get(i));
                }
            } else {
                
                throw new UnificationException("Cannot unify (by class)", adaptApp, fixType);
            }
            return null;
        }

        @Override
        public Void visit(TypeVariable adaptVar, Type fixType)
                throws TermException {
            if (fixType instanceof SchemaType) {
                fixType.accept(this, adaptVar);
            } else {
                // we can simply use the equality check here since type variables
                // have no arguments
                if(!adaptVar.equals(fixType)) {
                    throw new UnificationException("Cannot unify", adaptVar, fixType);
                }
            }
            return null;
        }

        @Override
        public Void visit(SchemaType stv, Type fixType)
                throws TermException {
            if (instantiation.containsKey(stv.getVariableName())) {
                instantiate(stv).accept(this, fixType);
            } else {
                Type inst = instantiate(fixType);
                if(!stv.equals(inst)) {
                    if (occursIn(stv, inst)) {
                        throw new UnificationException("Cannot unify (occur check)",
                                stv, inst);
                    }
                    addMapping(stv, inst);
                }
            }
            return null;
        }
    }
    
    private Unifier unifier = new Unifier();
    
    

    /*
     * helper function to determine whether a type variable may be instantiated
     * when type checking the application of terms to a function symbol
     * some type variables may not be instantiated.
     * 
     * Example:
     *   For a given polymorphic function symbol "bool f('a)" and a parameter
     *   type 'something the application "f(3)" should not be possible, since
     *   'something and int are not compatible. 
     *    
     * Type variants are made so that the check is 'a against '#something.
     * With '#something being immutable, the unification will fail.
     *   
     * Also: 'a and '#a are different avoiding name clashes.
     * 
     * ---
     * 
     * Bound type variables may also not specialised
     * 
     * Example:
     *   (\ALL_ty ''a; true as ''a) is illegal, since ''a would be bound to bool.
     */
//    private boolean isImmutableTypeVariable(TypeVariable tv) {
//        String variableName = tv.getVariableName();
//        return variableName.startsWith(TypeVariable.VARIANT_PREFIX) ||
//            variableName.startsWith(TypeVariable.BINDABLE_PREFIX);
//    }

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
            type1.accept(unifier, type2);
            assert instantiate(type1).equals(instantiate(type2)) : type1 + " vs " + type2;
            return instantiate(type1);
        } catch (TermException ex) {
            // restore old mapping
            instantiation = copy;
            
            assert ex instanceof UnificationException :
                "Only unification exception may be thrown here";
        
            UnificationException uex = (UnificationException) ex;
            uex.addDetail("Cannot left-unify \"" + type1 + "\" and \""
                    + type2 + "\"");
            
            throw uex;
        }
    }
    
    /**
     * Adds a mapping.
     * 
     * We have to update all existing mappings afterwards. We apply the new assignment
     * to all instantiations but to the one to type.
     * 
     * @param tv the schema type variable.
     * @param type the type to instantiate for it.
     */
    protected void addMapping(final @NonNull SchemaType tv,
            final @NonNull Type type) throws UnificationException {

        String variableName = tv.getVariableName();
        
        assert instantiation.get(variableName) == null;
        assert !occursIn(tv, type);

        instantiation.put(variableName, type);
        
        TypeVisitor<Type, Void> stvInst = new RebuildingTypeVisitor<Void>() {
            public Type visit(SchemaType typeVariable, Void param) {
                if(typeVariable.equals(tv)) {
                    return type;
                } else {
                    return typeVariable;
                }
            };
        };
        
        // TODO can we not use the "instantiator" now?
        // check whether this is needed or not: it is :)
        for (String t : instantiation.keySet()) {
            if(!variableName.equals(t)) {
                Type res;
                try {
                    res = instantiation.get(t).accept(stvInst, null);
                } catch (TermException e) {
                    // not thrown in that code
                    throw new Error(e);
                }
                instantiation.put(t, res);
            }
        }

    }

    private static TypeVisitor<Void, SchemaType> schemaDetector = new DefaultTypeVisitor<SchemaType>() {
        @Override
        public Void visit(SchemaType stv1, SchemaType stv2)
                throws TermException {
            if (stv1.equals(stv2))
                throw new TermException("SchemaTypeVariable found!");
            return null;
        }
    };

    /*
     * Occur check. Does tv appear in type?
     */
    private boolean occursIn(final SchemaType stv, Type type) {
        try {
            type.accept(schemaDetector, stv);
            // no exception: not found
            return false;
        } catch (TermException e) {
            // exception: schema type variable has been found
            return true;
        }
    }

    /**
     * get the instantiation map which maps type variables to types.
     * 
     * @return a unmodifiable mapping from type variables to maps.
     */
    public Map<String, Type> getInstantiation() {
        return Collections.unmodifiableMap(instantiation);
    }
    
    @Override
    public String toString() {
        return instantiation.toString();
    }

}
