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

import de.uka.iti.pseudo.term.statement.Statement;

/**
 * The exception UnificationException is used to propagate errors that happened during
 * unification/matching of two syntactical entities.
 * 
 * It usually does not indicate an error state but rather a failed unification try.
 *  
 * Messages can be added to allow more thorough inspection of errors. 
 */

@SuppressWarnings("serial") 
public class UnificationException extends TermException {
    
    private List<String> details = new LinkedList<String>();

	public UnificationException() {
		super();
	}

	public UnificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnificationException(String message) {
		super(message);
	}

	public UnificationException(Throwable cause) {
		super(cause);
	}

	public UnificationException(String message, Type t1, Type t2) {
	    this(message);
	    addDetail("Type 1: " + t1);
	    addDetail("Type 2: " + t2);
	}
	
	public UnificationException(String message, Term t1, Term t2) {
	    this(message);
	    addDetail("Term 1: " + t1);
	    addDetail("Term 2: " + t2);
	}

	public UnificationException(Term t1, Term t2) {
	    this("Fail to unify");
        addDetail("Term 1: " + t1);
        addDetail("Term 2: " + t2);
    }

    public UnificationException(String string, Statement s1, Statement s2) {
        this(string);
        addDetail("Statement 1: " + s1);
        addDetail("Statement 2: " + s2);
    }

    public void addDetail(String detail) {
	    details.add(detail);
	}

	public String getDetailedMessage() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(super.getMessage());
	    for (String detail : details) {
            sb.append("\n").append(detail);
        }
		return sb.toString();
	}
	
	// lets assume detailled messaged are always welcome
	public String getMessage() {
	    return getDetailedMessage();
	}

}
