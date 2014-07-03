package de.uka.iti.pseudo.auto.script.command;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.auto.script.ProofScriptCommand;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.proof.ProofNode;

public class AutoProofCommand implements ProofScriptCommand {

    @Override public String getName() {
        // TODO Implement AutoProofCommand.getName
        return null;
    }

    @Override public void checkSyntax(ProofScriptNode node) throws StrategyException {
        // TODO Implement AutoProofCommand.checkSyntax

    }

    @Override public List<ProofNode> apply(Map<String, String> arguments, ProofNode proofNode)
            throws StrategyException {
        // TODO Implement AutoProofCommand.apply
        return null;
    }

}
