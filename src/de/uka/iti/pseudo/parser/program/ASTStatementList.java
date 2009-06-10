package de.uka.iti.pseudo.parser.program;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTStatementList extends ASTElement {

    public ASTStatementList(List<ASTStatement> list) {
        assert list.size() >= 1;
        addChildren(list);
    }
    
    /*
     * get the location token of the first statement
     */
    public Token getLocationToken() {
        return getChildren().get(0).getLocationToken();
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
