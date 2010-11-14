package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;

/**
 * An assignment of one or more assignable locations.
 * 
 * @author timm.felden@felden.com
 */
public final class AssignmentStatement extends Statement {

    private final List<SimpleAssignment> assignments;

    public AssignmentStatement(List<SimpleAssignment> assignments) {
        super(assignments.get(0).name);
        
        this.assignments = assignments;

        addChildren(assignments);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<SimpleAssignment> getAssignments() {
        return assignments;
    }

}
