package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.Arrays;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;

public class MockProofHint implements ProofHint {

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env, List<String> arguments) {
        return new MockHintRuleAppFinder(env, arguments);
    }

    @Override
    public String getName() {
        return "mock";
    }

}

class MockHintRuleAppFinder extends HintRuleAppFinder {

    private Environment env;

    @Override
    public String toString() {
        return Arrays.toString(arguments);
    }

    public MockHintRuleAppFinder(Environment env, List<String> arguments) {
        super(arguments);
        this.env = env;
    }

    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) {
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setProofNode(node);
        ram.setRule(env.getRule("oops"));
        return ram;
    }

}
