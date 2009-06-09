package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SchemaCollectorVisitor;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;

// TODO DOC

public class RuleApplication {

    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private TermSelector[] assumeSelectors;
    private Pair<String, Term>[] termInstantiations;
    private Pair<String, Modality>[] modalityInstantiations;
    private Map<String, String> whereProperties;
    
    public RuleApplication(@NonNull Rule rule, 
            int goalNumber,
            @NonNull TermSelector findSelector, 
            @Nullable TermSelector[] assumeSelectors,
            @Nullable Map<String, Term> termInstantiations,
            @Nullable Map<String, Modality> modalityInstantiations) {
        super();
        this.rule = rule;
        this.goalNumber = goalNumber;
        this.findSelector = findSelector;
        this.assumeSelectors = assumeSelectors;
        
        SchemaCollectorVisitor scv = new SchemaCollectorVisitor();
        scv.collect(rule);
        
        this.termInstantiations = copyInst(termInstantiations, scv.getSchemaVariables());
        this.modalityInstantiations = copyInst(modalityInstantiations, scv.getSchemaModalities());
        
        // TODO check all this ... nö
    }
    
    // used for mock objects
    protected RuleApplication() {
        
    }
    
    @SuppressWarnings("unchecked") 
    private <E> Pair<String, E>[] copyInst(Map<String, E> from, Set<String> identifiers) {
        List<Pair<String, E>> retval = new ArrayList<Pair<String, E>>();
        if(from != null) {
            for (String id : identifiers) {
                retval.add(new Pair<String, E>(id, from.get(id)));
            }
        }
        return (Pair<String,E>[])Util.listToArray(retval, Pair.class);
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
    
    public List<Pair<String, Term>> getTermInstantiations() {
        return Util.readOnlyArrayList(termInstantiations);
    }
    
    public List<Pair<String, Modality>> getModalityInstantiations() {
        return Util.readOnlyArrayList(modalityInstantiations);
    }
    
    // this is used by the GUI
    @Override 
    public String toString() {
        return "Apply " + rule.getName();
    }

    public String getWhereProperty(String key) {
        if(whereProperties == null)
            return null;
        else
            return whereProperties.get(key);
    }
    
    public Collection<String> getWherePropertyNames() {
        return whereProperties.keySet();
    }
}
