package de.uka.iti.pseudo.term;

/**
 * Representation of the location of an object inside a program. This is mostly
 * used to provide useful information for the user.
 * 
 * @author timm.felden@felden.com
 */
public class CodeLocation {
    private final int line;
    /**
     * This object is used as representation of the program. Objects are not
     * restricted to ivil programs, but can as well be representations of source
     * language programs.
     */
    private final Object program;

    public CodeLocation(int line, final Object program) {
        this.line = line;
        this.program = program;
    }

    /**
     * Checks if two locations are equivalent.
     */
    public boolean sameAs(CodeLocation c) {
        return c.program != null && program != null && c.getLine() == getLine() && c.program.equals(program);
    }

    public Object getProgram() {
        return program;
    }

    public int getLine() {
        return line;
    }
}
