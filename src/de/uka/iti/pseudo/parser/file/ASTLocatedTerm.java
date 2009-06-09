package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;

public class ASTLocatedTerm extends ASTElement {
    
    private MatchingLocation matchingLocation;

    public ASTLocatedTerm(ASTTerm rt, MatchingLocation matchingLocation) {
        this.matchingLocation = matchingLocation;
        addChild(rt);
    }

    @Override 
    public Token getLocationToken() {
        return getChildren().get(0).getLocationToken();
    }

    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public MatchingLocation getMatchingLocation() {
        return matchingLocation;
    }
    
    public ASTTerm getTerm() {
        return (ASTTerm) getChildren().get(0);
    }

}
