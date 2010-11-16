package de.uka.iti.pseudo.term;


// TODO DOC
public class CodeLocation {
    private final int line;
    private final Object program;

    public CodeLocation(int line, final Object program) {
        this.line = line;
        this.program = program;
    }

    // FIXME Better rename that method since it might be confused with Object#equals(Object)
    public boolean equals(CodeLocation c) {
        return c.program != null && program != null && c.getLine() == getLine() && c.program.equals(program);
    }

    public Object getProgram() {
        return program;
    }

    public int getLine() {
        return line;
    }
}
