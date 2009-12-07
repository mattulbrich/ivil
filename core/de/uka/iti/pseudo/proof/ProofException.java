/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.proof;

/**
 * The Class ProofException is used by classes in the context of a {@link Proof}
 * and {@link ProofNode} to indicate a failure.
 */

@SuppressWarnings("serial")
public class ProofException extends Exception {

    public ProofException() {
        super();
    }

    public ProofException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProofException(String message) {
        super(message);
    }

    public ProofException(Throwable cause) {
        super(cause);
    }

}
