package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTAssignmentStatement extends ASTStatement {

    public ASTAssignmentStatement(Token target, ASTTerm term) {
        super(target);
        addChild(term);
    }
    
    public Token getTarget() {
        return firstToken;
    }
    
    public ASTTerm getTerm() {
        return (ASTTerm) getChildren().get(0);
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
