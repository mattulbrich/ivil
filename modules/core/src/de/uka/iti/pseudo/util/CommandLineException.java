/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

/**
 * Exception used by {@link CommandLine}.
 *
 * @see CommandLine
 */
@SuppressWarnings("serial")
public class CommandLineException extends Exception {

    /**
     * Instantiates a new command line exception without message.
     */
    public CommandLineException() {
        super();
    }

    /**
     * Instantiates a new command line exception.
     *
     * @param message
     *            an error message
     * @param cause
     *            the exception causing this exception
     */
    public CommandLineException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new command line exception.
     *
     * @param message
     *            an error message
     */
    public CommandLineException(String message) {
        super(message);
    }

    /**
     * Instantiates a new command line exception.
     *
     * @param cause
     *            the exception causing this exception
     */
    public CommandLineException(Throwable cause) {
        super(cause);
    }

}
