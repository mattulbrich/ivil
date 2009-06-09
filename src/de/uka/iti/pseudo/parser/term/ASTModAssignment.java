package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTModAssignment extends ASTModality {
    
    private Token assignedIdentifier;
    private ASTTerm assignedTerm;

    public ASTModAssignment(Token assignedIdentifier, ASTTerm assignedTerm) {
        this.assignedIdentifier = assignedIdentifier;
        this.assignedTerm = assignedTerm;
        addChild(assignedTerm);
    }
    
    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public final Token getAssignedIdentifier() {
        return assignedIdentifier;
    }

    public final ASTTerm getAssignedTerm() {
        return assignedTerm;
    }

}
