package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTAssignmentStatement extends ASTStatement {

    public ASTAssignmentStatement(ASTTerm target, ASTTerm term) {
        super(target.getLocationToken());
        addChild(target);
        addChild(term);
    }
    
    public ASTTerm getTarget() {
        return (ASTTerm) getChildren().get(0);
    }
    
    public boolean isSchema() {
        return getTarget() instanceof ASTSchemaVariableTerm;
    }
    
    public ASTTerm getTerm() {
        return (ASTTerm) getChildren().get(1);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
