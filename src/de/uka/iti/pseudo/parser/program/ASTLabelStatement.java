package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTLabelStatement extends ASTStatement {
    
    public ASTLabelStatement(Token label) {
        super(label);
    }

    public Token getLabel() {
        return firstToken;
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
