/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.TermMatcher;

// TODO DOC
// producer pattern?

public class RuleApplicationMaker implements RuleApplication {

    private Rule rule;
    private ProofNode proofNode;
    private TermSelector findSelector;
    private final Stack<TermSelector> assumeSelectors = new Stack<TermSelector>();
    private TermMatcher termMatcher;
    private final Map<String, String> properties = new HashMap<String, String>();

    public RuleApplicationMaker(Environment env) {
        // TODO Perhaps set this to null here?
        termMatcher = new TermMatcher();
    }

    public void setProofNode(ProofNode proofNode) {
        this.proofNode = proofNode;
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

    public void setTermMatcher(TermMatcher mc) {
        termMatcher = mc;
    }

    @Override
    public List<TermSelector> getAssumeSelectors() {
        return assumeSelectors;
    }

    @Override
    public TermSelector getFindSelector() {
        return findSelector;
    }

    @Override
    public ProofNode getProofNode() {
        return proofNode;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public Map<String, Term> getSchemaVariableMapping() {
        return termMatcher.getTermInstantiation();
    }

    @Override
    public Map<String, Update> getSchemaUpdateMapping() {
        return termMatcher.getUpdateInstantiation();
    }

    @Override
    public Map<String, Type> getTypeVariableMapping() {
        return termMatcher.getTypeInstantiation();
    }

    @Override
    public boolean hasMutableProperties() {
        return true;
    }

    public void clearProperties() {
        getProperties().clear();
    }

    public TermMatcher getTermMatcher() {
        return termMatcher;
    }

    /**
     * Matches the clauses (find and assumptions) against their selected
     * counterparts of the proof node.
     *
     * We need to have set all assumption selectors, the find selector, the rule
     * and the proof node.
     *
     * This method may succeed even if the rule is not applicable afterwards:
     * This is because located terms are not checked.
     * @throws ProofException if the sequent does not match the rule entities.
     */
    public void matchInstantiations() throws ProofException {

        if(rule == null) {
            throw new ProofException("Matching with null rule");
        }

        if(proofNode == null) {
            throw new ProofException("Matching with null proof node");
        }

        Sequent sequent = proofNode.getSequent();

        LocatedTerm findClause = rule.getFindClause();
        if(findClause != null && findSelector != null) {
            Term findRule = findClause.getTerm();
            Term findSeq = findSelector.selectSubterm(sequent);
            termMatcher.leftMatch(findRule, findSeq);
        }

        int i=0;
        for (LocatedTerm assumption : rule.getAssumptions()) {
            Term assRule = assumption.getTerm();
            Term assSeq = assumeSelectors.get(i++).selectSubterm(sequent);
            termMatcher.leftMatch(assRule, assSeq);
        }

    }
}
