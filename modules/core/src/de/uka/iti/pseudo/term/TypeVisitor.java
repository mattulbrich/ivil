/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;


/**
 * Allow visiting also for type structures. Even though this is a rather small
 * distinction it can make since in combination with for instance TermVisitor.
 *
 * @param <R> Result type for the methods
 * @param <P> Parameter type for the additional parameter to methods.
 */

// Checkstyle: OFF JavadocMethod - it is clear
public interface TypeVisitor</*@Nullable*/R, /*@Nullable*/P> {

    R visit(TypeApplication typeApplication, P parameter) throws TermException;

    R visit(TypeVariable typeVariable, P parameter) throws TermException;

    R visit(SchemaType schemaType, P parameter) throws TermException;
}
