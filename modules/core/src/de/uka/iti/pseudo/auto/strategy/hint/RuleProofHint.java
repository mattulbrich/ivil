package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.Rule;

/**
 * The Class RuleProofHint implements a proof hint which applies one rule.
 * 
 * @ivildoc "Proof hint/rule"
 * 
 * <h2>Proof hint <code>cut</code></h2>
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
    public String getKey() {
        return "rule";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new RuleHintAppFinder(env, arguments);
    }
}

class RuleHintAppFinder extends HintRuleAppFinder {

    private final Rule rule;
    private final Environment env;

    public RuleHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;
        
        if(arguments.size() != 2) {
            throw new StrategyException("The proofhint 'rule' expects exactly one argument");
        }
        
        assert "rule".equals(arguments.get(0));
        String ruleName = arguments.get(1);
        this.rule = env.getRule(ruleName);
        
        if(rule == null) {
            throw new StrategyException("Unknown rule in proof hint: " + ruleName);
        }
    }

    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {

        // Only applicable to the reasonNode, not later
        if(node != reasonNode) {
            return null;
        }

        // TODO FIND SOMETHING TO MATCH AGAINST 
        // last or first of succedent?
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setProofNode(node);
        ram.setRule(rule);
        return ram;
    }
    
}