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

import java.util.List;

import nonnull.Nullable;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;

// TODO DOC
// TODO rebuild the type only if at least one element has been changed
public class RebuildingTypeVisitor</*@Nullable*/ A> implements TypeVisitor</*@Nullable*/ Type, A> {

    public Type visit(TypeApplication typeApplication, A parameter) throws TermException {
        Sort sort = typeApplication.getSort();
        List<Type> arguments = typeApplication.getArguments();
        Type result[] = new Type[arguments.size()];
        for (int i = 0; i < result.length; i++) {
            Type ty = arguments.get(i).accept(this, parameter);
            assert ty != null : "nullness: rebuilding must not create null values";
            result[i] = ty;
        }
        
        return TypeApplication.getInst(sort, result);
    }

    public Type visit(TypeVariable typeVariable, A parameter) throws TermException {
        return typeVariable;
    }
    
    public Type visit(SchemaType schemaType, A parameter) throws TermException {
        return schemaType;
    }

}
