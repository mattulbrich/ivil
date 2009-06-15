package de.uka.iti.pseudo.parser.program;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTGotoStatement extends ASTStatement {

    public ASTGotoStatement(Token kw, List<ASTTerm> targets) {
        super(kw);
        addChildren(targets);
    }
    
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
