package de.uka.iti.pseudo.gui.extensions;

import java.util.Stack;

import de.uka.iti.pseudo.auto.strategy.RewriteRuleCollection;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;

public class SplitPropositionalExtension implements ContextExtension {

    @Override
    public String getName() {
        return "Splitting Expansion";
    }

    @Override
    public String getDescription() {
        return "safsdf";
    }

    @Override
    public boolean shouldOffer(ProofCenter proofCenter) {
        return !proofCenter.getCurrentProofNode().isClosed();
    }

    @Override
    public void run(ProofCenter proofCenter) throws Exception {
        Stack<ProofNode> nodes = new Stack<ProofNode>();
        nodes.push(proofCenter.getCurrentProofNode());
        Environment env = proofCenter.getEnvironment();

        RewriteRuleCollection rrc = new RewriteRuleCollection(env.getAllRules(),
                "split", env);

        while(!nodes.isEmpty()) {
            ProofNode n = nodes.pop();
            RuleApplicationMaker ra = rrc.findRuleApplication(n);
            if(ra != null) {
                proofCenter.getProof().apply(ra, env);
                nodes.addAll(n.getChildren());
            }
        }
    }

}
