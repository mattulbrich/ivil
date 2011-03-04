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

import nonnull.DeepNonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

/**
 * Captures a <code>goto</code> statement whose semantics is to
 * indeterministically branch the execution and jump to other instructions
 * within the program. It is one of the basic statements.
 * 
 * <p>
 * A <tt>goto</tt> statement takes one or more arguments. Each argument must be
 * an integer literal, or in case of a rule context, a schema variable.
 */
public class GotoStatement extends Statement {

    /**
     * Instantiates a new goto statement.
     * 
     * @param sourceLineNumber
     *            the source line number to set for this statement
     * @param targets
     *            the targets to jump to.
     * 
     * @throws TermException
     *             if targets is empty or contains a non-literal integer or a
     *             non-integer schema variable.
     */
    public GotoStatement(int sourceLineNumber, @DeepNonNull Term[] targets) throws TermException {
        super(sourceLineNumber, targets);
        
        checkGotoArguments();
    }

    /*
     * ensure that all arguments are either integer literals or integer schema
     * variables.
     */
    private void checkGotoArguments() throws TermException {
        
        if(countSubterms() == 0) {
            throw new TermException("A goto statement needs to have at least one argument.");
        }
        
        for (Term t : getSubterms()) {
            if (!t.getType().equals(Environment.getIntType())) {
                throw new TermException(
                        "Arguments to goto statements need to be of integer type: "
                                + t.toString(true) + " - " + t.getType());
            }

            if (t instanceof Application) {
                Application app = (Application) t;
                Function f = app.getFunction();
                if (!(f instanceof NumberLiteral)) {
                    throw new TermException(
                            "Constant arguments to goto statements need to be literals: "
                                    + t.toString(true));
                }
                
            } else if (!(t instanceof SchemaVariable)) {
                throw new TermException(
                        "Arguments to goto statements need to be literals or schema variables: "
                                + t.toString(true));
            }
        }

    }

    public String toString(boolean typed) {
        return "goto " + Util.commatize(getSubterms(), typed);
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

        return new GotoStatement(getSourceLineNumber(), newSubterms);
    }

}
