package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTLocatedElement;

public class EnvironmentException extends ASTVisitException {

    public EnvironmentException() {
        //DOC
        super();
        // TODO Auto-generated constructor stub
    }

    public EnvironmentException(String message, Throwable cause) {
        //DOC
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public EnvironmentException(String message) {
        //DOC
        super(message);
        // TODO Auto-generated constructor stub
    }

    public EnvironmentException(Throwable cause) {
        //DOC
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public static EnvironmentException definedTwice(String what,
            ASTLocatedElement loc1, ASTLocatedElement loc2) {
        return new EnvironmentException(what + " has been defined twice:\n  " +
                loc1.getLocation() + "\n  " + loc2.getLocation());
    }

}
