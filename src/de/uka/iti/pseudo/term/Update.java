package de.uka.iti.pseudo.term;

import java.util.Arrays;
import java.util.List;

import nonnull.NonNull;

import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;

public class Update {

    public Update(@NonNull AssignmentStatement[] assignments) {
        this.assignments = assignments.clone();
    }

    public Update(List<AssignmentStatement> assignments) {
        this.assignments = Util.listToArray(assignments, AssignmentStatement.class);
    }

    /**
     * The assignments are stored in an array which is not changed.
     */
    private AssignmentStatement[] assignments;
    
    
    /*
     * we do not print our own typing. the typing of 
     * the inner term suffices.
     */
    public String toString(boolean typed) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < assignments.length; i++) {
            if (i == 0)
                sb.append("{ ");
            else
                sb.append(" || ");
            sb.append(assignments[i].toString(typed));
        }
        sb.append(" }");

        return sb.toString();
    }
    
    @Override public String toString() {
        return toString(Term.SHOW_TYPES);
    }
    
    /**
     * Gets the assignments of the update of this update term.
     * 
     * the call is delegated to the update
     * 
     * @return an immutable list of assignments
     */
    public List<AssignmentStatement> getAssignments() {
        return Util.readOnlyArrayList(assignments);
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Update) {
            Update up = (Update) obj;
            return Arrays.equals(assignments, up.assignments);
        }
        return false;
    }

}
