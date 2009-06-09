package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTGoalAction extends ASTFileElement {
    
    Token goalKind;

    public ASTGoalAction(Token t) {
        goalKind = t;
    }

    public ASTGoalAction(de.uka.iti.pseudo.parser.file.Token t,
            List<ASTRuleElement> list) {
        goalKind = t;
        addChildren(list);
    }

    @Override protected Token getLocationToken() {
        return goalKind;
    }

    @Override public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getGoalKind() {
        return goalKind;
    }

}
