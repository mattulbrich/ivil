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
 * Captures an <code>assume</code> statement whose semantics is to add a
 * property to the assumptions and success continue the execution. It is one of
 * the basic statements.
 * 
 * <p>
 * An <tt>assume</tt> statement takes exactly one boolean argument.
 */
public class AssumeStatement extends Statement {

    /**
     * Instantiates a new assume statement.
     * 
     * @param sourceLineNumber
     *            the source line number to set for this statement
     * @param conditionTerm
     *            the condition term to be used for the check
     * 
     * @throws TermException
     *             thrown if {@code conditionTerm} is not of boolean type.
     */
    public AssumeStatement(int sourceLineNumber, Term conditionTerm) throws TermException {
        super(sourceLineNumber, conditionTerm);
        ensureCondition();
    }

    public String toString(boolean typed) {
        return "assume "
                + getSubterms().get(0).toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    
}
