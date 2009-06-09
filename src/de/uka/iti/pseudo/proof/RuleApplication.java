package de.uka.iti.pseudo.proof;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class RuleApplication {

    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private TermSelector assumeSelectors[];
    private Pair<SchemaVariable, Term> interactiveInstantiations[];
    public Rule getRule() {
        return rule;
    }
    public int getGoalNumber() {
        return goalNumber;
    }
    public TermSelector getFindSelector() {
        return findSelector;
    }
    public TermSelector[] getAssumeSelectors() {
        return assumeSelectors;
    }
    public Pair<SchemaVariable, Term>[] getInteractiveInstantiations() {
        return interactiveInstantiations;
    }
                                 
    
}
