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

package de.uka.iti.pseudo.term;

/**
 * Exceptions of type TermException are thrown during term creation, 
 * by {@link TermVisitor}s, {@link ModalityVisitor}s to indicate that 
 * some typing / unification / construction issue has been raised. 
 */
public class TermException extends Exception {

    private static final long serialVersionUID = -5653150088341377728L;

    public TermException() {
		super();
	}

	public TermException(String message, Throwable cause) {
		super(message, cause);
	}

	public TermException(String message) {
		super(message);
	}

	public TermException(Throwable cause) {
		super(cause);
	}

}
