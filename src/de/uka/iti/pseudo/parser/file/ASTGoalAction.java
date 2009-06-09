package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTGoalAction extends ASTFileElement {
    
    Token goalKind;
    Token name;

    public ASTGoalAction(Token t) {
        goalKind = t;
    }

    public ASTGoalAction(Token t, Token name, List<ASTRuleElement> list) {
        goalKind = t;
        this.name = name;
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

    public Token getName() {
        return name;
    }

}
