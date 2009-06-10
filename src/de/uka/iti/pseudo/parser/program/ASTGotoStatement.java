package de.uka.iti.pseudo.parser.program;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTGotoStatement extends ASTStatement {

    private List<Token> targets;

    public ASTGotoStatement(Token kw, List<Token> targets) {
        super(kw);
        this.targets = targets;
    }
    
    public List<Token> getTargets() {
        return targets;
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
