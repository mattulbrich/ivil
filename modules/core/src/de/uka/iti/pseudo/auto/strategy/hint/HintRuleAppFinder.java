package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;

import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.Util;

public abstract class HintRuleAppFinder {

    protected String[] arguments;

    /**
     * Instantiates a new hint.
     * 
     * You are allowed
     * @param arguments
     *            the arguments
     */
    public HintRuleAppFinder(List<String> arguments) {
        this.arguments = Util.listToArray(arguments, String.class);
    }

    public abstract RuleApplication findRuleApplication(ProofNode node, ProofNode reasonNode);

}
