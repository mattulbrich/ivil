/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;

/**
 * The Class RuleProofHint implements a proof hint which applies one rule.
 *
 * Must match with the first entry of succedent
 *
 * @ivildoc "Proof hint/rule"
 *
 * <h2>Proof hint <code>rule</code></h2>
 *
 * In order to perform a certain rule on a branch, this hint can be used.
 *
 * <h3>Arguments</h3>
 * cut takes one argument which is the name of the rule to be applied.
 *
 * <h3>Example</h3>
 * <pre>
 * assert emptyset &lt;: SetM ; "use the lemma §(rule emptyset_lemma)"
 * </pre>
 */
public class RuleProofHint implements ProofHint {

    @Override
    public String getName() {
        return "rule";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new RuleHintAppFinder(env, arguments);
    }
}

class RuleHintAppFinder extends HintRuleAppFinder {

    private final Set<ProofNode> appliedProofNodes = new HashSet<ProofNode>();
    private final Rule rules[];
    private final Environment env;

    public RuleHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;

        if(arguments.size() < 2) {
            throw new StrategyException("The proofhint 'rule' expects at least one argument");
        }

        assert "rule".equals(arguments.get(0));

        rules = new Rule[arguments.size() - 1];
        for (int i = 0; i < rules.length; i++) {
            String ruleName = arguments.get(i + 1);
            Rule rule = env.getRule(ruleName);
            if(rule == null) {
                throw new StrategyException("Unknown rule in proof hint: " + ruleName);
            }
            this.rules[i] = rule;
        }

    }

    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {

        // Only applicable to the reasonNode and direct successors if more than
        // one rule, not later
        int dist = distanceToReason(node, reasonNode);
        if(dist >= rules.length) {
            return null;
        }

        Rule rule = rules[dist];

        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setProofNode(node);
        ram.setRule(rule);
        if(rule.getFindClause() != null) {
            ram.setFindSelector(new TermSelector(TermSelector.SUCCEDENT, 0));
            try {
                ram.matchInstantiations();
            } catch (ProofException e) {
                throw new StrategyException("Error while instantiating rule " + rule.getName(), e);
            }
        }

        appliedProofNodes.add(node);
        return ram;
    }

    private int distanceToReason(ProofNode node, ProofNode reasonNode) {
        int result = 0;
        while(node != reasonNode) {
            if(appliedProofNodes.contains(node)) {
                result ++;
            }
            node = node.getParent();
            assert node != null : "The node is not a child of reasonNode!";
        }

        if(appliedProofNodes.contains(reasonNode)) {
            result ++;
        }

        return result;
    }

}