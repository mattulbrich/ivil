package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.statement.Statement;

public class LiteralProgramTerm extends ProgramTerm {
    
    private int programIndex;
    private Program program;

    public LiteralProgramTerm(@NonNull String image, boolean terminating,
            @NonNull Program program) throws TermException {
        super(terminating);
        this.program = program;
        try {
            this.programIndex = Integer.parseInt(image);
        } catch(NumberFormatException ex) {
            throw new TermException("Illegally formated literal program index: " + image, ex);
        }
        if(programIndex < 0)
            throw new TermException("Illegally formated literal program index: " + image);
    }
    
    /**
     * create a new literal program term which is a copy of the original 
     * program term with the exception of the programIndex which differs.
     *  
     * @param index program index of the new program term
     * @param original the original program term
     * @throws TermException if the index is invalid
     */
    public LiteralProgramTerm(int index, LiteralProgramTerm original) throws TermException {
        super(original.isTerminating());
        this.program = original.program;
        programIndex = index;
        
        if(programIndex < 0)
            throw new TermException("Illegally formated literal program index: " + index);
        
    }

    protected String getContentString(boolean typed) {
        return programIndex + ";" + program;
    }

    public boolean equals(Object object) {
        if (object instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) object;
            return programIndex == prog.programIndex &&
                program == prog.program &&
                isTerminating() == prog.isTerminating();
        }
        return false;
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public int getProgramIndex() {
        return programIndex;
    }

    public Statement getStatement() throws TermException {
        int programIndex = getProgramIndex();
        return program.getStatement(programIndex);
    }

    public Program getProgram() {
        return program;
    }
    
}
