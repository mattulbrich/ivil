/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.environment;


//TODO DOC
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
