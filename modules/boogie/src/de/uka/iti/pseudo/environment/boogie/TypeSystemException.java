package de.uka.iti.pseudo.environment.boogie;

import de.uka.iti.pseudo.parser.boogie.ParseException;

/**
 * This exception indicates a type error in a compilation unit.
 * 
 * @author timm.felden@felden.com
 */
public class TypeSystemException extends ParseException {

    private static final long serialVersionUID = -5476199340702432720L;

    public TypeSystemException(String msg) {
        super(msg);
    }

    public TypeSystemException(String msg, Exception e) {
        super(msg, e);
    }

}
