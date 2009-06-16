package de.uka.iti.pseudo.proof;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO DOC
// producer pattern?

public class RuleApplicationMaker implements RuleApplication {
    
    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private Stack<TermSelector> assumeSelectors = new Stack<TermSelector>();
    private TermUnification termUnification;
    private Map<String, String> properties = new HashMap<String, String>();
    
    public RuleApplicationMaker(Environment env) {
        termUnification = new TermUnification(env);
    }

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

    public void setTermUnification(TermUnification mc) {
        termUnification = mc;
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

    public Rule getRule() {
        return rule;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, Term> getSchemaVariableMapping() {
        return termUnification.getTermInstantiation();
    }

    public Map<String, Type> getTypeVariableMapping() {
        return termUnification.getTypeUnification().getInstantiation();
    }

    public boolean hasMutableProperties() {
        return true;
    }

    public void clearProperties() {
        getProperties().clear();
    }

    public TermUnification getTermUnification() {
        return termUnification;
    }
}
