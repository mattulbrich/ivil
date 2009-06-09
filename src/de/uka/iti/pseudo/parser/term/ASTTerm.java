package de.uka.iti.pseudo.parser.term;

import java.util.List;

public abstract class ASTTerm extends ASTElement {
    
    private List<ASTTerm> subterms; 

    public ASTTerm(List<ASTTerm> subterms) {
        this.subterms = subterms;
        addChildren(subterms);
    }

    public List<ASTTerm> getSubterms() {
        return subterms;
    }

}
