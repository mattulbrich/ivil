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
import de.uka.iti.pseudo.environment.Lemma;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.where.AxiomCondition;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.TermException;

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
public class AxiomProofHint implements ProofHint {

    @Override
    public String getName() {
        return "axiom";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new AxiomHintAppFinder(env, arguments);
    }
}

class AxiomHintAppFinder extends HintRuleAppFinder {

    private final Set<ProofNode> appliedProofNodes = new HashSet<ProofNode>();
    private final Rule addAxiomRule;
    private final Lemma axioms[];
    private final Environment env;

    public AxiomHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;

        if(arguments.size() < 2) {
            throw new StrategyException("The proofhint 'axiom' expects at least one argument");
        }

        assert "axiom".equals(arguments.get(0));

        addAxiomRule = env.getRule("axiom");
        if(addAxiomRule == null) {
            throw new StrategyException("Rule 'axiom' not known!");
        }

        axioms = new Lemma[arguments.size() - 1];
        for (int i = 0; i < axioms.length; i++) {
            String axName = arguments.get(i + 1);
            Lemma axiom = env.getLemma(axName);
            if(axiom == null) {
                throw new StrategyException("Unknown axiom in proof hint: " + axName);
            }
            this.axioms[i] = axiom;
        }


    }

    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {

        // Only applicable to the reasonNode and direct successors if more than
        // one rule, not later
        int dist = distanceToReason(node, reasonNode);
        if(dist >= axioms.length) {
            return null;
        }

        Lemma axiom = axioms[dist];

        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setProofNode(node);
        ram.setRule(addAxiomRule);
        try {
            SchemaVariable b = SchemaVariable.getInst("%b", Environment.getBoolType());
            ram.getTermMatcher().addInstantiation(b, axiom.getTerm());
            ram.getProperties().put(AxiomCondition.AXIOM_NAME_PROPERTY, axiom.getName());
            ram.matchInstantiations();
        } catch (ProofException e) {
            throw new StrategyException("Error while instantiating rule " + axiom.getName(), e);
        } catch (TermException e) {
            throw new StrategyException("Error while instantiating rule " + axiom.getName(), e);
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