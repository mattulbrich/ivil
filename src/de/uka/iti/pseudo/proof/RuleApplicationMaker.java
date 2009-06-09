package de.uka.iti.pseudo.proof;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO DOC
// proucer pattern?

public class RuleApplicationMaker implements RuleApplication {
    
    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private Stack<TermSelector> assumeSelectors = new Stack<TermSelector>();
    private Map<String, Term> schemaVariableInstantiations;
    private Map<String, Modality> schemaModalityInstantiations;
    private Properties whereProperties = new Properties();
    
    public void setGoalNumber(int goalNumber) {
        this.goalNumber = goalNumber;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public void setFindSelector(TermSelector findSelector) {
        this.findSelector = findSelector;
    }

    public void pushAssumptionSelector(TermSelector termSelector) {
       assumeSelectors.push(termSelector); 
    }

    public RuleApplication make() {
        return new ImmutableRuleApplication(this);
    }

    public void popAssumptionSelector() {
        assumeSelectors.pop();
    }

    public void setSchemaVariableInstantiations(
            Map<String, Term> schemaVariableInstantiations) {
        this.schemaVariableInstantiations = schemaVariableInstantiations;
    }

    public void setSchemaModalityInstantiations(
            Map<String, Modality> schemaModalityInstantiations) {
        this.schemaModalityInstantiations = schemaModalityInstantiations;
    }

    public void getInstantiationsFrom(TermUnification mc) {
        setSchemaVariableInstantiations(mc.getTermInstantiation());
        setSchemaModalityInstantiations(mc.getModalityInstantiation());
    }

    @Override public List<TermSelector> getAssumeSelectors() {
        return assumeSelectors;
    }

    @Override public TermSelector getFindSelector() {
        return findSelector;
    }

    @Override public int getGoalNumber() {
        return goalNumber;
    }

    @Override public Rule getRule() {
        return rule;
    }

    @Override public String getWhereProperty(String key) {
        return whereProperties.getProperty(key);
    }

    @Override public Collection<String> getWherePropertyNames() {
        return whereProperties.stringPropertyNames();
    }

    @Override public Modality getModalityInstantiation(String schemaModalityName) {
        return schemaModalityInstantiations.get(schemaModalityName);
    }

    @Override public Collection<String> getSchemaModalityNames() {
        return schemaModalityInstantiations.keySet();
    }

    @Override public Collection<String> getSchemaVariableNames() {
        return schemaVariableInstantiations.keySet();
    }

    @Override public Term getTermInstantiation(String schemaVariableName) {
        return schemaVariableInstantiations.get(schemaVariableName);
    }

    public Properties getWhereProperties() {
        return whereProperties;
    }

}
