package de.uka.iti.pseudo.proof;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation needed
public class MutableRuleApplication implements RuleApplication {
    
    private Rule rule;
    private List<TermSelector> assumeSelectors;
    private TermSelector findSelector;
    private int goalNumber;
    private Map<String, String> properties;
    private Map<String, Modality> schemaModalityMapping;
    private Map<String, Term> schemaVariableMapping;
    private Map<String, Type> typeVariableMapping;
    
    public MutableRuleApplication(RuleApplication selected) {
        // TODO!
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

    public Map<String, Modality> getSchemaModalityMapping() {
        return schemaModalityMapping;
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

    public boolean isMutable() {
        return true;
    }

}
