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

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

/**
 * The Class TypeVariableCollector provides the method {@link #collect(Type)} to
 * collect all appearing type variables into a set.
 * 
 * The method {@link #collect(Term)} applies collect to the types of all
 * subterms.
 */
public class TypeVariableCollector implements TypeVisitor {
    
    private TermVisitor typeVariableTermVisitor = new DefaultTermVisitor.DepthTermVisitor() {
        protected void defaultVisitTerm(Term term) throws TermException {
            super.defaultVisitTerm(term);
            term.getType().visit(TypeVariableCollector.this);
        }
        public void visit(Binding binding) throws TermException {
            super.visit(binding);
            binding.getVariableType().visit(TypeVariableCollector.this);
        }
    };
    
    /** 
     * Collect type variables in a term. All (indirect) subterms are visited and all
     * type variables are collected.
     */
    public static @NonNull Set<TypeVariable> collect(@NonNull Term term) {
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
    public static @NonNull Set<TypeVariable> collect(@NonNull Type type) {
        TypeVariableCollector tvc = new TypeVariableCollector();
        try {
            type.visit(tvc);
        } catch (TermException e) {
            // never thrown in the code
            throw new Error(e);
        }
        return tvc.typeVariables;
    }

    private Set<TypeVariable> typeVariables = new HashSet<TypeVariable>();

    /*
     * constructor hidden
     */
    private TypeVariableCollector() {
    }

    /*
     * add the type variable to the set
     */
    public Type visit(TypeVariable typeVariable) throws TermException {
        typeVariables.add(typeVariable);
        return null;
    }

    /*
     * visit all subexpressions
     */
    public Type visit(TypeApplication typeApplication) throws TermException {
        for (Type t : typeApplication.getArguments()) {
            t.visit(this);
        }
        return null;
    }

}
