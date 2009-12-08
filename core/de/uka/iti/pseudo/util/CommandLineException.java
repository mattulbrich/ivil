package de.uka.iti.pseudo.util;

/**
 * Exception used by {@link CommandLine}
 *
 * @see CommandLine
 */
@SuppressWarnings("serial") 
public class CommandLineException extends Exception {

    public CommandLineException() {
        super();
    }

    public CommandLineException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandLineException(String message) {
        super(message);
    }

    public CommandLineException(Throwable cause) {
        super(cause);
    }

}
