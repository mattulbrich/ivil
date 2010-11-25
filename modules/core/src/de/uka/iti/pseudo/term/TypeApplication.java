/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class TypeApplication models the application of a type constructor to a
 * number (possible 0) of types. The type constructor is given by a {@link Sort}
 * object.
 * 
 * @see TypeVariable
 * @see SchemaType
 * 
 * @author mattias ulbrich
 */
public class TypeApplication extends Type {
    
    /**
     * All type applications with no arguments share this constant.
     */
    private static final Type[] NO_ARGS = new Type[0];
    
    /**
     * The type parameters to be used with the constructor (i.e. the sort)
     */
    private Type[] typeParameters;
    
    /**
     * The sort to be used with the parameters.
     */
    private Sort sort;

    /**
     * Instantiates a new type application.
     * 
     * @param sort
     *            the sort
     * @param typeParameters
     *            the type parameters to use
     * 
     * @throws TermException
     *             if the arity of the sort does not match the number of
     *             paraameters.
     */
    public TypeApplication(@NonNull Sort sort, @DeepNonNull Type[] typeParameters) throws TermException {
    	
    	if(sort.getArity() != typeParameters.length)
    		throw new TermException("Sort " + sort.getName() + " expects " + sort.getArity() +
    				" parameters, but received " + typeParameters.length);
    	
        this.sort = sort;
        this.typeParameters = typeParameters;
    }
    
    /**
     * Instantiates a new type application without parameters
     * 
     * @param sort
     *            a nullary sort
     * 
     * @throws TermException
     *             if sort is not nullary.
     */
    public TypeApplication(@NonNull Sort sort) throws TermException {
        this(sort, NO_ARGS);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sort.getName());
        for (int i = 0; i < typeParameters.length; i++) {
            sb.append(i == 0 ? "(" : ",");
            sb.append(typeParameters[i]);
        }
        if(typeParameters.length > 0)
            sb.append(")");
        
        return sb.toString();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof TypeApplication) {
            TypeApplication tya = (TypeApplication) obj;
            if (tya.sort != sort)
                return false;
            for (int i = 0; i < typeParameters.length; i++) {
                if (!tya.typeParameters[i].equals(typeParameters[i]))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public <R,A> R accept(TypeVisitor<R,A> visitor, A parameter) throws TermException {
        return visitor.visit(this, parameter);
    }

    /**
     * Accept a type visitor and apply it to all type parameters.
     * 
     * @param visitor
     *            the visitor to accept
     * @param arg
     *            the parameter to the visitor
     * 
     * @throws TermException
     *             may be thrown during the visitation of the types.
     */
    public <R, A> void acceptDeep(TypeVisitor<R, A> visitor, A arg)
            throws TermException {
        for (Type param : typeParameters) {
            param.accept(visitor, arg);
        }
    }

    /**
     * Gets the sort of this type application.
     * 
     * @return the sort of the application
     */
    public @NonNull Sort getSort() {
        return sort;
    }

    /**
     * Gets the type parameters as an unmodifiable list.
     * 
     * @return a list whose length is the arity of {@link #getSort()}.
     */
    public List<Type> getArguments() {
        return Util.readOnlyArrayList(typeParameters);
    }
}
