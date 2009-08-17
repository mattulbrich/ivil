/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term;

/**
 * The TermVisitor interface can be implemented to create an instance which can
 * operate on term objects.
 * <p>
 * It is there to provide the visitor pattern.
 */
public interface TermVisitor {

    void visit(Variable variable) throws TermException;

    void visit(Binding binding) throws TermException;

    void visit(Application application) throws TermException;

    void visit(SchemaVariable schemaVariable) throws TermException;

    void visit(SchemaProgramTerm schemaProgramTerm) throws TermException;

    void visit(LiteralProgramTerm literalProgramTerm) throws TermException;

    void visit(UpdateTerm updateTerm) throws TermException;

    void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException;

}
