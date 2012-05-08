package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

/**
 * The base class for all hint strategy extensions.
 * 
 * To implement a proof hint plugin, this class needs to be extended. It does
 * the actual decision on rule applications.
 * 
 * You need to override the method
 * {@link #findRuleApplication(ProofNode, ProofNode)} to implement a hint rule
 * finder.
 * 
 * @see ProofHint
 */
public abstract class HintRuleAppFinder {

    /**
     * The arguments during construction.
     * 
     * You may work on these as you please.
     */
    protected String[] arguments;

    /**
     * Instantiates a new hint.
     * 
     * The given arguments are copied to an array.
     * 
     * @param arguments
     *            the textual arguments from the parser
     */
    public HintRuleAppFinder(List<String> arguments) {
        Log.log(Log.VERBOSE, "A hint rule app finder is created with arguments %s", 
                arguments);
        this.arguments = Util.listToArray(arguments, String.class);
    }

    /**
     * Find rule application.
     * 
     * Like {@link Strategy#findRuleApplication(ProofNode)} this method results
     * in a rule application for the given {@link ProofNode} {@code node}.
     * 
     * The additionally given proofnode <code>reasonNode</code> holds a
     * reference to the proof node when the hint was introduced (usually the
     * branch point of an assertion). This information can be used in the
     * calculation, too.
     * 
     * @param node
     *            the node to apply the rule application to
     * @param reasonNode
     *            the node at which the hint appeared.
     * @return a configured rule application for the given proof node.
     * @throws StrategyException
     *             if the finder is in an illegal state, it may choose to throw
     *             an exception.
     */
    public abstract RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException;

}
