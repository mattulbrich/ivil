package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTProblemSequent extends ASTElement {

    private int countSuccedent;
    private int countAntecedent;

    public ASTProblemSequent(List<ASTTerm> antecedent, List<ASTTerm> succedent) {
        this.countAntecedent = antecedent.size();
        this.countSuccedent = succedent.size();
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

}
