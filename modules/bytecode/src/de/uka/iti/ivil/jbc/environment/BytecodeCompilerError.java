package de.uka.iti.ivil.jbc.environment;

/**
 * This class is used to present hard compiler errors, that are not fixable and
 * thus do not have to be caught and handled in any way. They will usually be
 * caught by the loader and displayed as load error.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class BytecodeCompilerError extends RuntimeException {
    private static final long serialVersionUID = -2896865441984750018L;

    public BytecodeCompilerError() {
        super();
    }

    public BytecodeCompilerError(String msg) {
        super(msg);
    }

    public BytecodeCompilerError(Throwable cause) {
        super(cause);
    }

    public BytecodeCompilerError(String msg, Throwable cause) {
        super(msg, cause);
    }

}
