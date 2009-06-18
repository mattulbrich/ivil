package de.uka.iti.pseudo.term;

import java.util.Arrays;
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
    
    public UpdateTerm(List<AssignmentStatement> assignments, Term term) {
        this(Util.listToArray(assignments, AssignmentStatement.class), term);
    }

    public boolean equals(Object object) {
        if (object instanceof UpdateTerm) {
            UpdateTerm ut = (UpdateTerm) object;
            return Arrays.equals(assignments, ut.assignments) &&
                    getSubterm(0).equals(ut.getSubterm(0));
        }
        return false;
    }

    public String toString(boolean typed) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < assignments.length; i++) {
            if(i == 0) 
                sb.append("{ ");
            else
                sb.append(" || ");
            sb.append(assignments[i].toString(typed));
        }
        sb.append(" }");
        
        if(typed)
            sb.append("(").append(getSubterm(0).toString(true)).append(")");
        else
            sb.append(getSubterm(0).toString(false));
        
        return sb.toString();
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public List<AssignmentStatement> getAssignments() {
        return Util.readOnlyArrayList(assignments);
    }

}
