package de.uka.iti.pseudo.util;

import java.io.PrintStream;
import java.util.List;

/**
 * This exception is used as a container for many exceptions.
 */
public class CompoundException extends Exception {
    
    private static final long serialVersionUID = -2174383160531654448L;
    private Exception[] embeddedExceptions;

    public CompoundException(Exception[] exceptions) {
        this(Util.readOnlyArrayList(exceptions));
    }

    
    public CompoundException(List<Exception> exceptions) {
        this.embeddedExceptions = new Exception[exceptions.size()];
        exceptions.toArray(embeddedExceptions);
    }

    @Override
    public String getMessage() {
        switch(embeddedExceptions.length) {
        case 0:
            return "Empty compound exception";
        case 1:
            return embeddedExceptions[0].getMessage();
        default:
            return embeddedExceptions.length + " exceptions have occurred.";
        }
    }
    
    @Override
    public void printStackTrace(PrintStream s) {
        for(int i = 0; i < embeddedExceptions.length; i++) {
            s.println("--- embedded exception " + i + " ---");
            embeddedExceptions[i].printStackTrace(s);
        }
    }
}
