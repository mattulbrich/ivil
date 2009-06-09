package de.uka.iti.pseudo.proof;

import java.util.Stack;

import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;

// TODO DOC
// proucer pattern?

public class RuleApplicationMaker {
    
    private Rule rule;
    private int goalNumber;
    private TermSelector findSelector;
    private Stack<TermSelector> assumeSelectors = new Stack<TermSelector>();
    private Pair<SchemaVariable, Term> interactiveInstantiations[];
    
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
        return new RuleApplication(rule, goalNumber, findSelector, Util.listToArray(assumeSelectors, TermSelector.class), null);
    }

    public void popAssumptionSelector() {
        assumeSelectors.pop();
    }

}
