package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTSchematicAssignmentStatement extends ASTStatement {

    public ASTSchematicAssignmentStatement(Token token) {
        super(token);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getIdentifierToken() {
        return getLocationToken();
    }

}
