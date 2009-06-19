package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTHavocStatement extends ASTStatement {

    public ASTHavocStatement(Token kw, ASTTerm term) {
        super(kw);
        addChild(term);
        
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTTerm getArgument() {
        return (ASTTerm) getChildren().get(0);
    }

}
