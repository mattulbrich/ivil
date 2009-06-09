package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTLocatedTerm extends ASTFileElement {
    
    private MatchingLocation matchingLocation;

    public ASTLocatedTerm(ASTRawTerm rt, MatchingLocation matchingLocation) {
        this.matchingLocation = matchingLocation;
        addChild(rt);
    }

    @Override protected Token getLocationToken() {
        return getChildren().get(0).getLocationToken();
    }

    @Override public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public MatchingLocation getMatchingLocation() {
        return matchingLocation;
    }
    
    public ASTRawTerm getTerm() {
        return (ASTRawTerm) getChildren().get(0);
    }

}
