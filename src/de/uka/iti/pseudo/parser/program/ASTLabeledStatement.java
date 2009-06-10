package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTLabeledStatement extends ASTStatement {
    
    public ASTLabeledStatement(Token label, ASTStatement st) {
        super(label);
        addChild(st);
    }

    public Token getLabel() {
        return firstToken;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
