package de.uka.iti.pseudo.proof;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;

public class FilterRuleApplication implements RuleApplication {

    protected final RuleApplication ruleApp;

    public FilterRuleApplication(RuleApplication ruleApp) {
        this.ruleApp = ruleApp;
    }

    public Rule getRule() {
        return ruleApp.getRule();
    }

    public boolean hasMutableProperties() {
        return ruleApp.hasMutableProperties();
    }

    public ProofNode getProofNode() {
        return ruleApp.getProofNode();
    }

    public TermSelector getFindSelector() {
        return ruleApp.getFindSelector();
    }

    public List<TermSelector> getAssumeSelectors() {
        return ruleApp.getAssumeSelectors();
    }

    public Map<String, Term> getSchemaVariableMapping() {
        return ruleApp.getSchemaVariableMapping();
    }

    public Map<String, Update> getSchemaUpdateMapping() {
        return ruleApp.getSchemaUpdateMapping();
    }

    public Map<String, Type> getTypeVariableMapping() {
        return ruleApp.getTypeVariableMapping();
    }

    public Map<String, String> getProperties() {
        return ruleApp.getProperties();
    }

}
