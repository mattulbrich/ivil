package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class ModifiesClause extends Specification {
    
    // tokens are needed to link havoc statements to boogie code
    private final List<String> targets;

    public ModifiesClause(Token first, List<Token> targets) {
        super(first);

        this.targets = ASTConversions.toEscapedNameList(targets);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<String> getTargets() {
        return targets;
    }

}
