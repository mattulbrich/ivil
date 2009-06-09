package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Function;

public class AssignModality extends Modality {
    
    private Function assignedConstant;
    private Term assignedTerm;
    
    public AssignModality(Function assignedConstant,
            Term assignedTerm) throws TermException {
        super();
        this.assignedConstant = assignedConstant;
        this.assignedTerm = assignedTerm;
        
        check();
    }

    private void check() {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString(boolean typed) {
        return assignedConstant.getName() + ":=" + assignedTerm.toString(typed);
    }

}
