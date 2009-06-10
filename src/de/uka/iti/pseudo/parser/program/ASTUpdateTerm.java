package de.uka.iti.pseudo.parser.program;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTUpdateTerm extends ASTTerm {

    public ASTUpdateTerm(List<ASTAssignmentStatement> assignments, ASTTerm term) {
        super(Collections.singletonList(term));
        addChildren(assignments);
    }

    @Override public Token getLocationToken() {
        return getChildren().get(0).getLocationToken();
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
