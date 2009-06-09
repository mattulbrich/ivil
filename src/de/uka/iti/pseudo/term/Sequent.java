package de.uka.iti.pseudo.term;

import java.util.List;

import de.uka.iti.pseudo.util.Util;

// TODO DOC
public class Sequent {

    private Term[] antecedent;
    
    private Term[] succedent;
    
    public Sequent(Term[] antecedent, Term[] succedent) {
        this.antecedent = antecedent;
        this.succedent = succedent;
    }

    public List<Term> getAntecedent() {
        return Util.readOnlyArrayList(antecedent);
    }
    
    public List<Term> getSuccedent() {
        return Util.readOnlyArrayList(succedent);
    }
    
}
