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
