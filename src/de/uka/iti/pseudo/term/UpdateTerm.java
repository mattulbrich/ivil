package de.uka.iti.pseudo.term;

import java.util.List;

import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;

public class UpdateTerm extends Term {

    private AssignmentStatement[] assignments;

    public UpdateTerm(AssignmentStatement[] assignments, Term term) {
        super(new Term[] { term }, term.getType());
        this.assignments = assignments;
        
        assert assignments.length > 0;
    }

    @Override public boolean equals(Object object) {
        // TODO Implement UpdateTerm.equals
        return false;
    }

    @Override public String toString(boolean typed) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < assignments.length; i++) {
            if(i == 0) 
                sb.append("{ ");
            else
                sb.append(" || ");
            sb.append(assignments[i].toString(typed));
        }
        sb.append(" }").append(getSubterm(0).toString(typed));
        
        return sb.toString();
    }

    @Override public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public List<AssignmentStatement> getAssignments() {
        return Util.readOnlyArrayList(assignments);
    }

}
