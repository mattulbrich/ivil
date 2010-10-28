package de.uka.iti.pseudo.term;


public class CodeLocation {
    public final int line;
    public final Object program;

    CodeLocation(int line, final Object program) {
        this.line = line;
        this.program = program;
    }

    public boolean equals(CodeLocation c) {
        return c.program != null && program != null && c.line == line && c.program.equals(program);
    }
}
