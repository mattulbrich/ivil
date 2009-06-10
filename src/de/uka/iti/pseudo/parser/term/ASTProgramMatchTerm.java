package de.uka.iti.pseudo.parser.term;

import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.term.ASTProgramLabel;
import de.uka.iti.pseudo.parser.term.ASTProgramTerm;

public class ASTProgramMatchTerm extends ASTProgramTerm {

    public ASTProgramMatchTerm(ASTProgramLabel position,
            boolean termination, ASTStatement matchStatement) {
        super(position, termination);
        addChild(matchStatement);
    }

    /*
     * statement is there as first child element 
     */
    public @Nullable ASTStatement getMatchStatement() {
        List<ASTElement> children = getChildren();
        return (ASTStatement) children.get(0);
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    
}
