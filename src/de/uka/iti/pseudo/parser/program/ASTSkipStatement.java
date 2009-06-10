package de.uka.iti.pseudo.parser.program;

import java.util.List;

import nonnull.Nullable;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTSkipStatement extends ASTStatement {

    public ASTSkipStatement(Token keyWord, ASTTerm term) {
        super(keyWord);
        addChild(term);
    }

    public ASTSkipStatement(Token keyWord) {
        super(keyWord);
    }

    /*
     * skip may or may not have a formula!
     */
    public @Nullable ASTTerm getTerm() {
        List<ASTElement> children = getChildren();
        if(children.size() > 0)
            return (ASTTerm) children.get(0);
        else
            return null;
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
