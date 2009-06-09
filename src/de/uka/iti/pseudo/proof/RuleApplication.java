package de.uka.iti.pseudo.proof;

import nonnull.NonNull;
import nonnull.Nullable;
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
    
    public RuleApplication(@NonNull Rule rule, 
            int goalNumber,
            @NonNull TermSelector findSelector, 
            @Nullable TermSelector[] assumeSelectors,
            @Nullable Pair<SchemaVariable, Term>[] interactiveInstantiations) {
        super();
        this.rule = rule;
        this.goalNumber = goalNumber;
        this.findSelector = findSelector;
        this.assumeSelectors = assumeSelectors;
        this.interactiveInstantiations = interactiveInstantiations;
        
        // TODO check all this ... nö
    }
    
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
    
    // this is used by the GUI
    @Override 
    public String toString() {
        return "Apply " + rule.getName();
    }
    
}
