/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
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
import de.uka.iti.pseudo.term.TypeVariableBinding;
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

    /**
     * This class implements a term visitor which visits the tree in prefix
     * order.
     *
     * It can be used as base class for many specialised visitors which need
     * visitation in depth by overriding individual <tt>visit</tt> methods.
     */
    public static class DepthTermVisitor extends DefaultTermVisitor {


        /**
         * {@inheritDoc}
         * <p>
         * The depth visitor visits all subterms of the argument term in prefix
         * order.
         */
        @Override
        protected void defaultVisitTerm(Term term) throws TermException {
            for (Term t : term.getSubterms()) {
                t.visit(this);
            }
        }
    }

    /**
     * This method abstracts the default behaviour of the visitor. All
     * individual <tt>visit</tt> methods delegate to this method.
     *
     * @param term
     *            the term to visit
     * @throws TermException
     *             a term exception may be thrown by the implementation.
     */
    protected abstract void defaultVisitTerm(Term term) throws TermException;

    @Override
    public void visit(Variable variable) throws TermException {
        defaultVisitTerm(variable);
    }

    @Override
    public void visit(Binding binding) throws TermException {
        defaultVisitTerm(binding);
    }

    @Override
    public void visit(Application application) throws TermException {
        defaultVisitTerm(application);
    }

    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        defaultVisitTerm(schemaVariable);
    }

    @Override
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        defaultVisitTerm(schemaProgramTerm);
    }

    @Override
    public void visit(LiteralProgramTerm literalProgramTerm) throws TermException {
        defaultVisitTerm(literalProgramTerm);
    }

    @Override
    public void visit(UpdateTerm updateTerm) throws TermException {
        defaultVisitTerm(updateTerm);
    }

    @Override
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        defaultVisitTerm(schemaUpdateTerm);
    }

    @Override
    public void visit(TypeVariableBinding typeVariableBinding) throws TermException {
        defaultVisitTerm(typeVariableBinding);
    }

}
