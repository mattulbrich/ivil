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
package de.uka.iti.pseudo.parser;

import de.uka.iti.pseudo.parser.file.ASTFileVisitor;
import nonnull.Nullable;
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
    
    private @Nullable ASTLocatedElement location = null;

	public ASTVisitException(String message, ASTLocatedElement location) {
		super(message);
		this.location = location;
	}
	
	public ASTVisitException(String message, Throwable cause) {
		super(message, cause);
	}

	public ASTVisitException(String message) {
		super(message);
	}

	// do not include the cause's classname
	public ASTVisitException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
	public ASTVisitException(String message, ASTLocatedElement location, Throwable cause) {
		super(message, cause);
		this.location = location;
	}

	public ASTVisitException(ASTLocatedElement location, Throwable cause) {
		this(cause);
		this.location = location;
	}

	@Override
	public @Nullable String getMessage() {
		if(location == null)
			return super.getMessage();
		else
			return super.getMessage() + " (" + location.getLocation() + ")";
	}

    public @Nullable ASTLocatedElement getLocation() {
        return location;
    }

}
