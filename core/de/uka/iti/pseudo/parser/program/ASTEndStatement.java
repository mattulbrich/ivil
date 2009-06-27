package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTEndStatement extends ASTStatement {

    public ASTEndStatement(Token keyWord, ASTTerm term) {
        super(keyWord);
        addChild(term);
    }

    public ASTTerm getTerm() {
        return (ASTTerm) getChildren().get(0);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
