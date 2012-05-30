/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie;

import de.uka.iti.pseudo.parser.file.ASTFileVisitor;

/**
 * This class of exceptions may be thrown during visiting using a
 * {@link ASTFileVisitor} or a {@link ASTVisitor}.
 * 
 * They have special constructors that allow to specify a location
 * which is then added to error message. The location could also be 
 * used separately.
 */
public class ASTVisitException extends Exception {

    private static final long serialVersionUID = 6326168209506163512L;
    
    private ASTElement location;

    public ASTVisitException(String message, ASTElement location) {
		super(message);
		this.location = location;
	}
	
	public ASTVisitException(String message, Throwable cause) {
		super(message, cause);
	}

	public ASTVisitException(String message) {
		super(message);
	}

	public ASTVisitException(Throwable cause) {
		super(cause);
	}
	
    public ASTVisitException(String message, ASTElement location, Throwable cause) {
		super(message, cause);
		this.location = location;
	}

    public ASTVisitException(ASTElement location, Throwable cause) {
		super(cause);
		this.location = location;
	}

	@Override
	public String getMessage() {
		if(location == null)
			return super.getMessage();
		else
			return super.getMessage() + " (" + location.getLocation() + ")";
	}

    public ASTElement getLocation() {
        return location;
    }

}
