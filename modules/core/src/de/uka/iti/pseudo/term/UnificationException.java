/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import java.util.LinkedList;
import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * The exception UnificationException is used to propagate errors that happened
 * during unification/matching of two syntactical entities.
 *
 * It usually does not indicate an error state but rather a failed unification
 * try.
 *
 * Messages can be added to allow more thorough inspection of errors.
 */

@SuppressWarnings("serial")
public class UnificationException extends TermException {

    /**
     * The details of the unification are kept as a list of strings.
     */
    private final List<String> details = new LinkedList<String>();

    /**
     * Instantiates a new unification exception.
     *
     * Details are kept empty
     */
    public UnificationException() {
        super();
    }

    /**
     * Instantiates a new unification exception. Details are kept empty.
     *
     * @param message
     *            the message
     * @param cause
     *            the causing exception
     */
    public UnificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new unification exception. Details are kept empty.
     *
     * @param message
     *            the message
     */
    public UnificationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new unification exception. Details are kept empty
     *
     * @param cause
     *            the causing exception
     */
    public UnificationException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new unification exception.
     *
     * <p>
     * Details are added to state the given arguments. Usually used when type
     * unification fails.
     *
     * @param message
     *            the message
     * @param t1
     *            an arbitrary type
     * @param t2
     *            an arbitrary type
     */
    public UnificationException(String message, Type t1, Type t2) {
        this(message);
        addDetail("Type 1: " + t1);
        addDetail("Type 2: " + t2);
    }

    /**
     * Instantiates a new unification exception.
     *
     * <p>
     * Details are added to state the given arguments. Usually used when term
     * unification fails.
     *
     * @param message
     *            the message
     * @param t1
     *            an arbitrary term
     * @param t2
     *            an arbitrary term
     */
    public UnificationException(String message, Term t1, Term t2) {
        this(message);
        addDetail("Term 1: " + t1);
        addDetail("Term 2: " + t2);
    }

    /**
     * Instantiates a new unification exception.
     *
     * <p>
     * Details are added to state the given arguments. Usually used when term
     * unification fails. The message is set to "Fail to unify"
     *
     * @param t1
     *            an arbitrary term
     * @param t2
     *            an arbitrary term
     */
    public UnificationException(Term t1, Term t2) {
        this("Fail to unify", t1, t2);
    }

    /**
     * Instantiates a new unification exception.
     *
     * <p>
     * Details are added to state the given arguments. Usually used when
     * statement unification fails.
     *
     * @param message
     *            the message
     * @param s1
     *            an arbitrary statement
     * @param s2
     *            an arbitrary statement
     */
    public UnificationException(String message, Statement s1, Statement s2) {
        this(message);
        addDetail("Statement 1: " + s1);
        addDetail("Statement 2: " + s2);
    }

    /**
     * Adds another detail to the description.
     *
     * @param detail
     *            a detail error message.
     */
    public void addDetail(String detail) {
        details.add(detail);
    }

    /**
     * Gets the detailed error message.
     *
     * <p>
     * This takes the original {@linkplain #getMessage() message} of the
     * exception and appends all recorded detail messages, each one on
     * a new line.
     *
     * @return the detailed exception message
     */
    public @NonNull String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessage());
        for (String detail : details) {
            sb.append("\n").append(detail);
        }
        return sb.toString();
    }

    // lets assume detailled messaged are always welcome
    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return getDetailedMessage();
    }

}
