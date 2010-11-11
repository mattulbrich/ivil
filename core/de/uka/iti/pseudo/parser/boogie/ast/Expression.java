package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

// TODO implement expressions
public class Expression extends ASTElement {

    private final Token first;

    // TODO delete
    public Expression() {
        first = null;
    }

    Expression(Token first) {
        this.first = first;
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        // TODO delete
    }

}
