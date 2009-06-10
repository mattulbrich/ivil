/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.rule;

/**
 * RuleExceptions are thrown when something goes wrong in the construction or
 * application of rules.
 * This happens for instance in class Rule itsself (or its composing things) 
 * or in where conditions.
 */
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
