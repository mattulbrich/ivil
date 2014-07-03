package de.uka.iti.pseudo.auto.script;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.auto.script.command.AutoProofCommand;
import de.uka.iti.pseudo.auto.script.command.YieldProofCommand;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Named;
import de.uka.iti.pseudo.proof.ProofNode;

// TODO DOC
public interface ProofScriptCommand extends Named {

    String SERVICE = "proofScriptCommand";
    ProofScriptCommand YIELD_COMMAND = new YieldProofCommand();
    ProofScriptCommand AUTO_COMMAND = new AutoProofCommand();

    public void checkSyntax(ProofScriptNode node) throws StrategyException;

    public List<ProofNode> apply(Map<String, String> arguments, ProofNode proofNode) throws StrategyException;

}
