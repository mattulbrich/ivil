package de.uka.iti.pseudo.term;

import java.util.Arrays;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.util.Util;

// TODO DOC
public class Sequent {

    private Term[] antecedent;
    
    private Term[] succedent;
    
    public Sequent(Term[] antecedent, Term[] succedent) throws TermException {
        this(Arrays.asList(antecedent), Arrays.asList(succedent));
    }

    public Sequent(List<Term> antecedent, List<Term> succedent) throws TermException {
        this.antecedent = Util.listToArray(antecedent, Term.class);
        this.succedent = Util.listToArray(succedent, Term.class);
        typeCheck();
    }

    private void typeCheck() throws TermException {
        for (Term t : antecedent) {
            if(!t.getType().equals(Environment.getBoolType())) {
                throw new TermException("Expecting boolean terms in sequents");
            }
        }
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
