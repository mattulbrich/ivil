package de.uka.iti.pseudo.parser.program;

import java.util.List;

import nonnull.Nullable;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTSkipStatement extends ASTStatement {

    public ASTSkipStatement(Token keyWord, List<ASTTerm> termList) {
        super(keyWord);
        addChildren(termList);
    }

    public ASTSkipStatement(Token keyWord) {
        super(keyWord);
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
