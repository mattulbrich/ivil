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

import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;

/**
 * The Class DefaultTermVisitor provides an abstract implementation of
 * {@link TermVisitor} in which all visit methods are delegated to
 * a default visit implementation.
 * 
 * The subclass {@link DepthTermVisitor} is not abstract and simply implements
 * the default visit method by a visitation of all children.
 */
public abstract class DefaultTermVisitor implements TermVisitor {
    
    public static class DepthTermVisitor extends DefaultTermVisitor {

        protected void defaultVisitTerm(Term term) throws TermException {
            for (Term t : term.getSubterms()) {
                t.visit(this);
            }
        }
    }

    protected abstract void defaultVisitTerm(Term term) throws TermException;
    
    public void visit(Variable variable) throws TermException {
        defaultVisitTerm(variable);
    }

    public void visit(Binding binding) throws TermException {
        defaultVisitTerm(binding);
    }

    public void visit(Application application) throws TermException {
        defaultVisitTerm(application);
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        defaultVisitTerm(schemaVariable);
    }

    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        defaultVisitTerm(schemaProgramTerm);
    }

    public void visit(LiteralProgramTerm literalProgramTerm) throws TermException {
        defaultVisitTerm(literalProgramTerm);
    }

    public void visit(UpdateTerm updateTerm) throws TermException {
        defaultVisitTerm(updateTerm);
    }
    
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        defaultVisitTerm(schemaUpdateTerm);
    }

}
