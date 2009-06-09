/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class EnvironmentException extends ASTVisitException {

    public EnvironmentException(String message) {
        super(message);
    }

    public EnvironmentException(String message, ASTLocatedElement location) {
        super(message, location);
    }

    public static EnvironmentException definedTwice(String what,
            ASTLocatedElement loc1, ASTLocatedElement loc2) {
        return new EnvironmentException(what + " has been defined twice:\n  " +
                loc1.getLocation() + "\n  " + loc2.getLocation());
    }

}
