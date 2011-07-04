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

package de.uka.iti.pseudo.environment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTypeVisitor;

/**
 * The Class TypeVariableCollector provides the method {@link #collect(Type)} to
 * collect all appearing type variables into a set.
 * 
 * The method {@link #collect(Term)} applies collect to the types of all
 * subterms.
 * 
 * One can also collect schema types.
 * 
 * The collection can furthermore also be applied to types instead of terms.
 * 
 * The collections (sets) which contain the entities are lazily created.
 */
public class TypeVariableCollector {

    private Set<TypeVariable> typeVariables = null;

    private Set<SchemaType> schemaTypeVariables = null;

    private TypeVisitor<Void, Void> typeVisitor = new DefaultTypeVisitor<Void>() {
        public Void visit(SchemaType schemaTypeVariable, Void argument) {
            if(schemaTypeVariables == null) {
                schemaTypeVariables = new HashSet<SchemaType>();
            }
            schemaTypeVariables.add(schemaTypeVariable);
            return null;
        };

        public Void visit(TypeVariable typeVariable, Void argument) {
            if(typeVariables == null) {
                typeVariables = new HashSet<TypeVariable>();
            }
            typeVariables.add(typeVariable);
            return null;
        };
    };

    private TermVisitor typeVariableTermVisitor = new DefaultTermVisitor.DepthTermVisitor() {
        protected void defaultVisitTerm(Term term) throws TermException {
            super.defaultVisitTerm(term);
            term.getType().accept(typeVisitor, null);
        }

        public void visit(Binding binding) throws TermException {
            super.visit(binding);
            binding.getVariableType().accept(typeVisitor, null);
        }
        
        public void visit(TypeVariableBinding typeVariableBinding) throws TermException {
            typeVariableBinding.getBoundType().accept(typeVisitor, null);
        }
    };

    /*
     * constructor hidden
     */
    private TypeVariableCollector() {
    }

    /**
     * Collect type variables in a term. All (indirect) subterms are visited and
     * all type variables are collected.
     * 
     * @param term
     *            some term whose types are to be searched for type variables
     * 
     * @return the set of type variable found in term.
     */
    public static @DeepNonNull Set<TypeVariable> collect(@NonNull Term term) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            term.visit(tvc.typeVariableTermVisitor);
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return makeSet(tvc.typeVariables);
    }

    /**
     * Collect type variables in a type.
     * 
     * @param type
     *            some arbitrary type
     * 
     * @return the set of type variable found in type.
     */
    public static @DeepNonNull Set<TypeVariable> collect(@NonNull Type type) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            type.accept(tvc.typeVisitor, null);
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return  makeSet(tvc.typeVariables);
    }
    
    /**
     * Collect type variables in a collection of types.
     * 
     * The collection is iterated and all found type variables are accumulated.
     * 
     * @param types
     *            a collection of types
     * 
     * @return the set of type variable found types.
     */
    public static @DeepNonNull Set<TypeVariable> collect(@DeepNonNull Collection<Type> types) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            for (Type type : types) {
                type.accept(tvc.typeVisitor, null);
            }
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return  makeSet(tvc.typeVariables);
    }

    /**
     * Collect schema type variables in a term. All (indirect) subterms are
     * visited and all schmema type variables are collected.
     * 
     * @param term
     *            some term whose types are to be searched for schema type
     *            variables
     * 
     * @return the set of schema type variable found in term.
     */
    public static @DeepNonNull Set<SchemaType> collectSchema(@NonNull Term term) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            term.visit(tvc.typeVariableTermVisitor);
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return  makeSet(tvc.schemaTypeVariables);
    }

    /**
     * Collect schema schema type variables in a type.
     * 
     * @param type
     *            some arbitrary type
     * 
     * @return the set of schema type variable found in type.
     */
    public static @DeepNonNull Set<SchemaType> collectSchema(@NonNull Type type) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            type.accept(tvc.typeVisitor, null);
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return makeSet(tvc.schemaTypeVariables);
    }
    
    /**
     * Collect schema type in a collection of types.
     * 
     * The collection is iterated and all found schema type are accumulated.
     * 
     * @param types
     *            a collection of types
     * 
     * @return the set of schema type found types.
     */
    public static Set<SchemaType> collectSchema(@DeepNonNull Collection<Type> types) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            for (Type type : types) {
                type.accept(tvc.typeVisitor, null);
            }
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return makeSet(tvc.schemaTypeVariables);
    }

    
    /**
     * Turns a <code>null</code> into an empty set.
     * 
     * @param <E>
     *            elements in the set
     * @param set
     *            set to handle
     * @return a reference to an empty set if {@code set==null} is true, {@code
     *         set} otherwise.
     */
    private static @NonNull <E> Set<E> makeSet(@Nullable Set<E> set) {
        if(set == null)
            return Collections.emptySet();
        else
            return set;
    }

}
