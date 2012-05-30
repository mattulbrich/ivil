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
 * This happens for instance in class Rule itsself (or its composing things) 
 * or in where conditions.
 */

@SuppressWarnings("serial")
public class RuleException extends Exception {

    public RuleException() {
        super();
    }

    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleException(String message) {
        super(message);
    }

    public RuleException(Throwable cause) {
        super(cause);
    }

}
