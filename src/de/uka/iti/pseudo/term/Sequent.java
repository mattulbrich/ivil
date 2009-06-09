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

    public Sequent(List<Term> antecedent, List<Term> succedent) {
        this.antecedent = Util.listToArray(antecedent, Term.class);
        this.succedent = Util.listToArray(succedent, Term.class);
    }

    public List<Term> getAntecedent() {
        return Util.readOnlyArrayList(antecedent);
    }
    
    public List<Term> getSuccedent() {
        return Util.readOnlyArrayList(succedent);
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Term t : antecedent) {
            sb.append(t).append(" ");
        }
        sb.append("|-");
        for (Term t : succedent) {
            sb.append(" ").append(t);
        }
        return sb.toString();
    }
    
}
