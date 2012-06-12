/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.TermException;

/**
 * This abstract class implements the {@link StatementVisitor} interface and
 * delegates every visitation to the abstract default implementation
 * {@link #visitDefault(Statement)} whose behaviour is defined by extending
 * subclasses.
 */

//Checkstyle: OFF JavadocMethod
public abstract class DefaultStatementVisitor implements StatementVisitor {

    /**
     * Default visitation method. Unless a visit method is overwritten in a
     * subclass this method will be called upon a call to any of the visit
     * method.
     *
     * @param statement
     *            a statement to visit.
     */
    protected abstract void visitDefault(Statement statement);

    @Override
    public void visit(AssertStatement assertStatement) throws TermException {
        visitDefault(assertStatement);
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) throws TermException {
        visitDefault(assignmentStatement);
    }

    @Override
    public void visit(AssumeStatement assumeStatement) throws TermException {
        visitDefault(assumeStatement);
    }

    @Override
    public void visit(EndStatement endStatement) throws TermException {
        visitDefault(endStatement);
    }

    @Override
    public void visit(GotoStatement gotoStatement) throws TermException {
        visitDefault(gotoStatement);
    }

    @Override
    public void visit(SkipStatement skipStatement) throws TermException {
        visitDefault(skipStatement);
    }

    @Override
    public void visit(HavocStatement havocStatement) throws TermException {
        visitDefault(havocStatement);
    }

}
