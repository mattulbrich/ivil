package de.uka.iti.ivil.jbc.environment;


/**
 * Exception used by ibc to signal any kind of problem.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class BytecodeCompilerException extends Exception {

    public BytecodeCompilerException(String string) {
        super(string);
    }

    public BytecodeCompilerException(Exception e) {
        super(e);
    }

    public BytecodeCompilerException(String string, Exception e) {
        super(string, e);
    }

    private static final long serialVersionUID = -5698837693240076560L;
}
