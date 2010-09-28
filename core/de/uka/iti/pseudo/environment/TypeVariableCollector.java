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

import java.util.HashSet;
import java.util.Set;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTypeVisitor;

/**
 * The Class TypeVariableCollector provides the method {@link #collect(Type)} to
 * collect all appearing type variables into a set.
 * 
 * The method {@link #collect(Term)} applies collect to the types of all
 * subterms.
 */
public class TypeVariableCollector {

    private Set<TypeVariable> typeVariables = new HashSet<TypeVariable>();

    private Set<SchemaType> schemaTypeVariables = new HashSet<SchemaType>();

    private TypeVisitor<Void, Void> typeVisitor = new DefaultTypeVisitor<Void>() {
        public Void visit(SchemaType schemaTypeVariable, Void argument)
                throws TermException {
            schemaTypeVariables.add(schemaTypeVariable);
            return null;
        };

        public Void visit(TypeVariable typeVariable, Void argument)
                throws TermException {
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
        return tvc.typeVariables;
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
        return tvc.typeVariables;
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
        return tvc.schemaTypeVariables;
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
        return tvc.schemaTypeVariables;
    }

}
