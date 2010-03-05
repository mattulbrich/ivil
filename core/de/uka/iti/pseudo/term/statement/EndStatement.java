/*
 * This file is part of PSEUDO
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
 * Captures an <code>end</code> statement whose semantics is to assert a
 * property and afterwards end the execution. <code>end phi</code> is equivalent
 * to the statement sequence <code>assert phi; assume false</code>. It is,
 * hence, a redundant feature and may be - at a later point - removed from the
 * set of core statements.
 * 
 * <p>
 * An <tt>end</tt> statement takes exactly one boolean argument.
 */
public class EndStatement extends Statement {

    /**
     * Instantiates a new end statement.
     * 
     * @param sourceLineNumber
     *            the source line number to set for this statement
     * @param conditionTerm
     *            the condition term to be used for the check
     * 
     * @throws TermException
     *             thrown if {@code conditionTerm} is not of boolean type.
     */
    public EndStatement(int sourceLineNumber, Term conditionTerm)
            throws TermException {
        super(sourceLineNumber, conditionTerm);
        ensureCondition();
    }

    public String toString(boolean typed) {
        return "end " + getSubterms().get(0).toString(typed);
    }

    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
