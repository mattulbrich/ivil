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
 * The TermVisitor interface can be implemented to create an instance which can
 * operate on term objects.
 * <p>
 * It is there to provide the visitor pattern.
 */
// Checkstyle: OFF JavadocMethod
public interface TermVisitor {

    void visit(Variable variable) throws TermException;

    void visit(Binding binding) throws TermException;

    void visit(Application application) throws TermException;

    void visit(SchemaVariable schemaVariable) throws TermException;

    void visit(SchemaProgramTerm schemaProgramTerm) throws TermException;

    void visit(LiteralProgramTerm literalProgramTerm) throws TermException;

    void visit(UpdateTerm updateTerm) throws TermException;

    void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException;

    void visit(TypeVariableBinding typeVariableBinding) throws TermException;

}
