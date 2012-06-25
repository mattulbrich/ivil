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

/**
 * Exceptions of type TermException are thrown during term creation, by
 * {@link TermVisitor}s, {@link ModalityVisitor}s to indicate that some typing /
 * unification / construction issue has been raised.
 */
public class TermException extends Exception {

    private static final long serialVersionUID = -5653150088341377728L;

    /**
     * Instantiates a new term exception without message.
     */
    public TermException() {
        super();
    }

    /**
     * Instantiates a new term exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the causing exception
     */
    public TermException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new term exception.
     *
     * @param message
     *            the message
     */
    public TermException(String message) {
        super(message);
    }

    /**
     * Instantiates a new term exception.
     *
     * @param cause
     *            the causing exception
     */
    public TermException(Throwable cause) {
        super(cause);
    }

}
