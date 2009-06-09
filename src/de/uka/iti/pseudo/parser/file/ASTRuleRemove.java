package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

// TODO Documentation needed
public class ASTRuleRemove extends ASTRuleElement {

    public ASTRuleRemove(Token first) {
        super(first);
    }

    @Override public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
