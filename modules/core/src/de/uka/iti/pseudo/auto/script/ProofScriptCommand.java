package de.uka.iti.pseudo.auto.script;

import java.util.List;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Named;
import de.uka.iti.pseudo.proof.ProofNode;

// TODO DOC
public interface ProofScriptCommand extends Named {

    String SERVICE = "proofScriptCommand";
    ProofScriptCommand YIELD_COMMAND = new YieldProofCommand();

    public void checkSyntax(ProofScriptNode node) throws StrategyException;

    public List<ProofNode> apply(ProofScriptNode node, ProofNode proofNode) throws StrategyException;

}
