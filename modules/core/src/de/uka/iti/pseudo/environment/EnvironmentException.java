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

package de.uka.iti.pseudo.environment;

/**
 * Exceptions of class EnvironmentException are mainly thrown 
 * by classes in the package de.uka.iti.pseudo.environment to 
 * indicate that something related to environment creation or
 * usage has gone wrong.
 */
public class EnvironmentException extends Exception {

    private static final long serialVersionUID = -7443118615154672177L;

    public EnvironmentException(String message) {
        super(message);
    }

    public EnvironmentException() {
        super();
    }

    public EnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnvironmentException(Throwable cause) {
        super(cause);
    }

}
