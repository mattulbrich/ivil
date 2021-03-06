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

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * Captures a <code>havoc</code> statement whose semantics is to set a variable
 * to an arbitrary value.
 *
 * <p>
 * A <tt>havoc</tt> statement takes exactly one argument which must be an
 * assignable or in the context of a rule, a schema variable (later to be
 * instantiated by an assignable).
 */
public final class HavocStatement extends Statement {

    /**
     * Instantiates a new havoc statement.
     *
     * @param sourceLineNumber
     *            the source line number
     * @param parameter
     *            the assignable function to havoc
     *
     * @throws TermException
     *             thrown if {@code parameter} is neither assignable nor a
     *             schema variable.
     */
    public HavocStatement(int sourceLineNumber, Term parameter) throws TermException {
        super(sourceLineNumber, parameter);

        if(!(parameter instanceof SchemaVariable)) {
            if(parameter instanceof Application) {
                Function f = ((Application)parameter).getFunction();
                if(!f.isAssignable()) {
                    throw new TermException("can havoc only an assignable function: " + parameter);
                }
            } else {
                throw new TermException("can havoc only assignables or schema variables: " +
                        parameter);
            }
        }

    }

    @Override
    public String toString(boolean typed) {
        // do never print "havoc n as int" but "havoc n"
        return "havoc " + getSubterms().get(0).toString(false);
    }

    @Override
    public void accept(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public Term getTarget() {
        return getSubterms().get(0);
    }

//    @Override
//    public Statement getWithReplacedSubterms(Term[] newSubterms) throws TermException {
//        assert newSubterms.length == 1;
//        if (newSubterms[0].equals(getSubterms().get(0))) {
//            return this;
//        }
//
//        return new HavocStatement(getSourceLineNumber(), newSubterms[0]);
//    }
}
