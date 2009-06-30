package de.uka.iti.pseudo.parser.file;

import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTGoalAction extends ASTElement {
    
    @Nullable Token goalKindToken;
    Token name;

    public ASTGoalAction(@NonNull Token t) {
        goalKindToken = t;
    }

    public ASTGoalAction(Token t, Token name, List<ASTRuleElement> list) {
        goalKindToken = t;
        this.name = name;
        addChildren(list);
    }

    public Token getLocationToken() {
        if(goalKindToken != null)
            return goalKindToken;
        else
            return getChildren().get(0).getLocationToken();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public @Nullable Token getGoalKindToken() {
        return goalKindToken;
    }

    public Token getName() {
        return name;
    }

}
