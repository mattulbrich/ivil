package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation needed
public class MutableRuleApplication implements RuleApplication {
    
    private Rule rule;
    private List<TermSelector> assumeSelectors;
    private TermSelector findSelector;
    private int goalNumber;
    private Map<String, String> properties;
    private Map<String, Term> schemaVariableMapping;
    private Map<String, Type> typeVariableMapping;
    
    public MutableRuleApplication(RuleApplication ruleApp) {
        this.rule = ruleApp.getRule();
        this.assumeSelectors = new ArrayList<TermSelector>(ruleApp.getAssumeSelectors());
        this.findSelector = ruleApp.getFindSelector();
        this.goalNumber = ruleApp.getGoalNumber();
        this.properties = new HashMap<String, String>(ruleApp.getProperties());
        this.schemaVariableMapping = new HashMap<String, Term>(ruleApp.getSchemaVariableMapping());
        this.typeVariableMapping = new HashMap<String, Type>(ruleApp.getTypeVariableMapping());
    }

    public MutableRuleApplication() {
        this.properties = new HashMap<String, String>();
        this.schemaVariableMapping = new HashMap<String, Term>();
        this.typeVariableMapping = new HashMap<String, Type>();
    }

    public void setFindSelector(TermSelector findSelector) {
        this.findSelector = findSelector;
    }

    public void setGoalNumber(int goalNumber) {
        this.goalNumber = goalNumber;
    }

    public List<TermSelector> getAssumeSelectors() {
        return assumeSelectors;
    }

    public TermSelector getFindSelector() {
        return findSelector;
    }

    public int getGoalNumber() {
        return goalNumber;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, Term> getSchemaVariableMapping() {
        return schemaVariableMapping;
    }

    public Map<String, Type> getTypeVariableMapping() {
        return typeVariableMapping;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public boolean hasMutableProperties() {
        return true;
    }

}
