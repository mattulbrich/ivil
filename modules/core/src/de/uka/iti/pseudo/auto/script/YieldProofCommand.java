package de.uka.iti.pseudo.auto.script;

import java.util.List;

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
    }

    @Override
    public List<ProofNode> apply(ProofScriptNode node, ProofNode proofNode) {
        return null;
    }

}
