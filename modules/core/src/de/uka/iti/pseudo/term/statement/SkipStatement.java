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

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

/**
 * Captures a <code>skip</code> statement whose semantics is to "do nothing".
 * 
 * <p>
 * A <tt>skip</tt> statement can take zero or more arbitrary arguments which may
 * be used as "hints" to the symbolic execution strategy.
 */
public class SkipStatement extends Statement {

    /**
     * Instantiates a new skip statement with a number of arguments.
     * 
     * @param sourceLineNumber
     *            the source line number to set for this statement
     * @param arguments
     *            zero or more arguments to the statement
     * 
     * @throws TermException
     *             currently not thrown
     */
    public SkipStatement(int sourceLineNumber, @NonNull Term[] arguments) throws TermException {
        super(sourceLineNumber, arguments);
    }
    
    /**
     * Instantiates a new skip statement without line number and without
     * arguments. This is a convenience constructor, used in tests.
     * 
     * @throws TermException
     *             currently not thrown
     */
    public SkipStatement() throws TermException {
        super(-1);
    }

    /**
     * {@inheritDoc}
     * 
     * If no arguments have been provided for this statement <code>"skip"</code>
     * is returned, otherwise the list of comma-separated arguments is prefixed
     * with <code>skip_loopinv</code> for legacy reasons.
     * 
     * TODO make this prefix to "skip" everywhere.
     * 
     * @param typed
     *            whether or not the argument types should also be printed.
     */
    public String toString(boolean typed) {
        if(countSubterms() == 0) {
            return "skip";
        } else {
            return "skip_loopinv " + Util.commatize(getSubterms(), typed);
        }
    }

    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    @Override
    public Statement getWithReplacedSubterms(Term[] newSubterms) throws TermException {
        if (newSubterms.length == getSubterms().size())
            throw new TermException("It is required to supply the same amount of subterms; was: "
                    + getSubterms().size() + " is: " + newSubterms.length);

        int i = 0;
        while (newSubterms[i].equals(getSubterms().get(i))) {
            i++;
            if (i == newSubterms.length)
                return this;
        }

        return new SkipStatement(getSourceLineNumber(), newSubterms);
    }

}
