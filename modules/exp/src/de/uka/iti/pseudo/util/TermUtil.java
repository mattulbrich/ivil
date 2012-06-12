package de.uka.iti.pseudo.util;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public final class TermUtil {

    private TermUtil() {
        assert false : "must not be instantiated";
    }

    public static int getIntLiteral(Term value) throws TermException {
        if(!(value instanceof Application)) {
            throw new TermException("expected a number literal, not " + value);
        }

        Function func = ((Application)value).getFunction();
        if(!(func instanceof NumberLiteral)) {
            throw new TermException("expected a number literal, not " + value);
        }

        // TODO check integer range
        // silently assuming it is in integer range ...
        int intValue = ((NumberLiteral)func).getValue().intValue();
        return intValue;
    }

    public static Function getFunction(Term value) throws TermException {
        if(!(value instanceof Application)) {
            throw new TermException("expected an application term, not " + value);
        }
        return ((Application)value).getFunction();
    }
}
