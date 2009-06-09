package de.uka.iti.pseudo.proof;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.util.LinearLookupMap;
import de.uka.iti.pseudo.util.Util;

// TODO DOC

public class ImmutableRuleApplication implements RuleApplication {

    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private TermSelector[] assumeSelectors;
    private Map<String,Term> schemaVariableMap;
    private Map<String,Modality> schemaModalityMap;
    private Map<String, String> properties;
    private Map<String,Type> typeVariableMap;
    
    public ImmutableRuleApplication(RuleApplication ruleApp) {
        rule = ruleApp.getRule();
        goalNumber = ruleApp.getGoalNumber();
        findSelector = ruleApp.getFindSelector();
        assumeSelectors = Util.listToArray(ruleApp.getAssumeSelectors(), TermSelector.class);
        
        schemaVariableMap = new LinearLookupMap<String, Term>(ruleApp.getSchemaVariableMapping());
        schemaModalityMap = new LinearLookupMap<String, Modality>(ruleApp.getSchemaModalityMapping());
        typeVariableMap = new LinearLookupMap<String, Type>(ruleApp.getTypeVariableMapping());
        properties = new LinearLookupMap<String, String>(ruleApp.getProperties());
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

    public List<TermSelector> getAssumeSelectors() {
        return Util.readOnlyArrayList(assumeSelectors);
    }

    // this is used by the GUI
    @Override public String toString() {
        return "Apply " + rule.getName();
    }

    public Map<String, Term> getSchemaVariableMapping() {
        return schemaVariableMap;
    }

    public Map<String, Modality> getSchemaModalityMapping() {
        return schemaModalityMap;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, Type> getTypeVariableMapping() {
        return typeVariableMap;
    }

    public boolean hasMutableProperties() {
        return false;
    }
}
