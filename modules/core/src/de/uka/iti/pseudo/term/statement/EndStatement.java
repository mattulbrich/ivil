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
 * Captures an <code>end</code> statement whose semantics is to end the execution.
 * A trace ending on an 'end' statement is true iff the last state makes the formula
 * after the program term true.
 *
 * <p>
 * An <tt>end</tt> statement takes no argument.
 */
public final class EndStatement extends Statement {

    /**
     * Instantiates a new end statement.
     *
     * @param sourceLineNumber
     *            the source line number to set for this statement
     * @throws TermException
     *             thrown if {@code conditionTerm} is not of boolean type.
     */
    public EndStatement(int sourceLineNumber)
            throws TermException {
        super(sourceLineNumber);
    }

    @Override
    public String toString(boolean typed) {
        return "end";
    }

    @Override
    public void accept(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

//    @Override
//    public Statement getWithReplacedSubterms(Term[] newSubterms) throws TermException {
//        assert newSubterms.length == 0;
//
//        return this;
//    }
}
