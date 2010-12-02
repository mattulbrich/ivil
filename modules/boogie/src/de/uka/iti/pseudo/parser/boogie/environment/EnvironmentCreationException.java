package de.uka.iti.pseudo.parser.boogie.environment;

import de.uka.iti.pseudo.parser.boogie.ParseException;

/**
 * This exception indicates an unexpected error during environment creation.
 * These should mostly point to a bug in the environment creation process
 * itself.
 * 
 * @author timm.felden@felden.com
 */
public class EnvironmentCreationException extends ParseException {

    private static final long serialVersionUID = 9217770502098071890L;

    public EnvironmentCreationException(String string) {
        super(string);
    }

}