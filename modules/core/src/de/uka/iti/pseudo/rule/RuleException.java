/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.rule;

/**
 * RuleExceptions are thrown when something goes wrong in the construction or
 * application of rules.
 * This happens for instance in class Rule itself (or its composing things)
 * or in where conditions.
 */

@SuppressWarnings("serial")
public class RuleException extends Exception {

    /**
     * Instantiates a new rule exception.
     */
    public RuleException() {
        super();
    }

    /**
     * Instantiates a new rule exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new rule exception.
     *
     * @param message
     *            the message
     */
    public RuleException(String message) {
        super(message);
    }

    /**
     * Instantiates a new rule exception.
     *
     * @param cause
     *            the cause
     */
    public RuleException(Throwable cause) {
        super(cause);
    }
}
