package de.uka.iti.pseudo.term;

import java.util.Arrays;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

public class LiteralProgramTerm extends ProgramTerm {
    
    private int programIndex;
    private ProgramUpdate[] updates;

    public LiteralProgramTerm(String image, boolean terminating,
            List<ProgramUpdate> updates) throws TermException {
        super(terminating);
        this.updates = Util.listToArray(updates, ProgramUpdate.class);
        try {
            programIndex = Integer.parseInt(image);
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
        this.updates = original.updates;
        programIndex = index;
        
        if(programIndex < 0)
            throw new TermException("Illegally formated literal program index: " + index);
        
    }

    protected String getContentString(boolean typed) {
        String result = Integer.toString(programIndex);
        for (ProgramUpdate upd : updates) {
            result += " || " + upd.toString(typed);
        }
        return result;
    }

    public boolean equals(Object object) {
        if (object instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) object;
            return programIndex == prog.programIndex &&
                Arrays.equals(updates, prog.updates);
        }
        return false;
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public int getProgramIndex() {
        return programIndex;
    }

    public Statement getStatement(Environment env) throws TermException {
        int programIndex = getProgramIndex();
        for (ProgramUpdate upd : updates) {
            if(programIndex == upd.getUpdatedIndex())
                return upd.getStatement();
        }
        return env.getProgram().getStatement(programIndex);
    }
    
    public List<ProgramUpdate> getUpdates() {
        return Util.readOnlyArrayList(updates);
    }

}
