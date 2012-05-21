package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTProblemSequent extends ASTDeclarationBlock {

    private final int countAntecedent;
    private final Token identifier;

    public ASTProblemSequent(Token firstToken, Token identifier,
            List<ASTTerm> antecedent, List<ASTTerm> succedent) {
        super(firstToken);
        this.identifier = identifier;
        this.countAntecedent = antecedent.size();
        addChildren(antecedent);
        addChildren(succedent);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return getChildren().get(0).getLocationToken();
    }

    public int getAntecedentCount() {
        return countAntecedent;
    }

    public Token getIdentifier() {
        return identifier;
    }

}
