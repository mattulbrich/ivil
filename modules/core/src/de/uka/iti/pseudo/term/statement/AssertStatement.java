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
package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * Captures an <code>assert</code> statement whose semantics is to assert a
 * property and in case of success continue the execution. It is one of the
 * basic statements.
 * 
 * <p>
 * An <tt>assert</tt> statement takes exactly one boolean argument.
 */
public class AssertStatement extends Statement {

    /**
     * Instantiates a new assert statement.
     * 
     * @param sourceLineNumber
     *            the source line number to set for this statement
     * @param conditionTerm
     *            the condition term to be used for the check
     * 
     * @throws TermException
     *             thrown if {@code conditionTerm} is not of boolean type.
     */
    public AssertStatement(int sourceLineNumber, Term conditionTerm) throws TermException {
        super(sourceLineNumber, conditionTerm);
        ensureCondition();
    }

    public String toString(boolean typed) {
        return "assert " + getSubterms().get(0).toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    @Override
    public Statement getWithReplacedSubterms(Term[] newSubterms) throws TermException {
        assert newSubterms.length == 1;
        if (newSubterms[0] == getSubterms().get(0))
            return this;

        return new AssertStatement(getSourceLineNumber(), newSubterms[0]);
    }

}
