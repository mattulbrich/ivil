package de.uka.iti.pseudo.auto.script.command;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.auto.script.ProofScriptCommand;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class CutProofScriptCommand implements ProofScriptCommand {

    @Override
    public String getName() {
        return "cut";
    }

    @Override
    public void checkSyntax(ProofScriptNode node) throws StrategyException {
        Map<String, String> args = node.getArguments();
        if(!args.containsKey("#1") || args.size() != 1) {
            throw new StrategyException("cut expects a sole formula argument");
        }
        if(node.getChildren().size() != 2) {
            throw new StrategyException("cut expects two children proof scripts");
        }
    }

    @Override
    public List<ProofNode> apply(Map<String, String> arguments, ProofNode proofNode)
            throws StrategyException {
        try {
            Proof proof = proofNode.getProof();
            Environment env = proof.getEnvironment();
            Rule cutRule = env.getRule("cut");
            if(cutRule == null) {
                throw new StrategyException("Cut rule not known");
            }
            RuleApplicationMaker ram = new RuleApplicationMaker(env);
            ram.setProofNode(proofNode);
            Term formula = TermMaker.makeAndTypeTerm(arguments.get("#1"),
                    proofNode.getLocalSymbolTable());
            ram.setRule(cutRule);
            ram.getTermMatcher().addInstantiation("%inst", formula);
            proof.apply(ram);

            return proofNode.getChildren();
        } catch (Exception e) {
            throw new StrategyException(e);
        }
    }

}
