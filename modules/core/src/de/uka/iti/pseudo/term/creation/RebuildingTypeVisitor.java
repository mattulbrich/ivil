/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.util.Util;

/**
 * A RebuildingTypeVisitor can be used to replace schematic types or type
 * variables in a type expression.
 * 
 * An implementing class would override {@link #visit(SchemaType, Object)}
 * and/or {@link #visit(TypeVariable, Object)}.
 * 
 * @param <A>
 *            An argument to the visitor which is passed through to all children
 *            of the type.
 */
public class RebuildingTypeVisitor<A> implements TypeVisitor<Type, A> {

    public Type visit(TypeApplication typeApplication, A parameter) throws TermException {
        Sort sort = typeApplication.getSort();
        List<Type> arguments = typeApplication.getArguments();
        Type newArgs[] = null;
        for (int i = 0; i < arguments.size(); i++) {
            Type arg = arguments.get(i);
            Type result = arg.accept(this, parameter);
            assert result != null : "nullness: Rebuilding must not return null type";
            
            if(result != arg) {
                if(newArgs == null) {
                    // copy the arguments only on demand.
                    newArgs = Util.listToArray(arguments, Type.class);
                }
                newArgs[i] = result;
            }
        }
        
        if(newArgs != null) {
            // rebuild the type only if at least one element has been changed
            return TypeApplication.getInst(sort, newArgs);
        } else {
            return typeApplication;
        }
    }

    public Type visit(TypeVariable typeVariable, A parameter) throws TermException {
        return typeVariable;
    }
    
    public Type visit(SchemaType schemaType, A parameter) throws TermException {
        return schemaType;
    }
}
