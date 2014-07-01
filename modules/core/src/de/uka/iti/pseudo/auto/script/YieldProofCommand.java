package de.uka.iti.pseudo.auto.script;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.proof.ProofNode;

public class YieldProofCommand implements ProofScriptCommand {

    @Override
    public String getName() {
        return "yield";
    }

    @Override
    public void checkSyntax(ProofScriptNode node) throws StrategyException {
        if(node.getArguments().size() != 0) {
            throw new StrategyException("yield command does not accept arguments");
        }
        if(!node.getChildren().isEmpty()) {
            throw new StrategyException("yield must not have children script nodes");
        }
    }

    @Override
    public List<ProofNode> apply(Map<String, String> arguments, ProofNode proofNode)
            throws StrategyException {
        return Collections.emptyList();
    }

}
