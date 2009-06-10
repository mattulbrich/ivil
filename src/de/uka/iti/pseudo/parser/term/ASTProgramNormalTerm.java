package de.uka.iti.pseudo.parser.term;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.term.ASTProgramLabel;
import de.uka.iti.pseudo.parser.term.ASTProgramTerm;

public class ASTProgramNormalTerm extends ASTProgramTerm {

    public ASTProgramNormalTerm(ASTProgramLabel position, boolean termination,
            List<ASTProgramUpdate> list) {
        super(position, termination);
        addChildren(list);
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
