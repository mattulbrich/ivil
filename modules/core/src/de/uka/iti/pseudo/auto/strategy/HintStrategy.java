/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.strategy.hint.HintParser;
import de.uka.iti.pseudo.auto.strategy.hint.HintRuleAppFinder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.util.Log;

/**
 * The Class HintStrategy implements a {@link Strategy} which is configured by
 * annotation in the program.
 *
 * <P>
 * Annotations (called 'hints') are added to the program code in the
 * annotiations to the statements, like assert phi ; "Annotation with §hint".
 * Parsing is done by class {@link HintParser}. See there for a description of
 * the format.
 *
 * <p>
 * When such a statement is symbolically executed, this strategy registers this,
 * parses the hints and stores the applicable {@link HintRuleAppFinder} for the
 * new branches in a map.
 *
 * <p>
 * When queried later for a rule application, the strategy checks the hints of
 * all parent nodes of a proof node and queries the according
 * {@link HintRuleAppFinder}.
 *
 * @see HintParser
 */
public final class HintStrategy extends AbstractStrategy {

    /**
     * The name of the corresponding plugin service as
     * mentioned in file PluginManager.properties.
     */
    public static final String PROOF_HINT_SERVICE_NAME = "proofHint";

    /**
     * The hint map stores the rule finder per proof node.
     */
    private final Map<ProofNode, List<HintRuleAppFinder>> hintMap =
            new HashMap<ProofNode, List<HintRuleAppFinder>>();

    /**
     * The hint parser is used to parse annotations.
     *
     * <p> Can be <code>null</code> until initialised.
     */
    private @Nullable HintParser hintParser;

    /**
     * A boolean flag how to handle errors: <code>true</code> means report them
     * as exceptions, <code>false</code> means log them only and then drop them.
     */
    private boolean raiseErrors = true;

    private Environment env;

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.auto.strategy.AbstractStrategy#init(de.uka.iti.pseudo.proof.Proof, de.uka.iti.pseudo.environment.Environment, de.uka.iti.pseudo.auto.strategy.StrategyManager)
     */
    @Override
    public void init(@NonNull Proof proof, @NonNull Environment env,
            @NonNull StrategyManager strategyManager) throws StrategyException {
        this.env = env;
        super.init(proof, env, strategyManager);
        this.hintParser = new HintParser(env);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Here we clear the map of hints.
     */
    @Override
    public void endSearch() {
        hintMap.clear();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * A rule application is found by searching the parent nodes of the given
     * node. If any of them is attributed with hits, the according
     * {@link HintRuleAppFinder} are inquired for a rule application. The first
     * found application is returned.
     */
    @Override
    public @Nullable RuleApplication findRuleApplication(ProofNode node)
            throws StrategyException {
        ProofNode reasonNode = node;
        while(reasonNode != null) {
            List<HintRuleAppFinder> hints = hintMap.get(reasonNode);
            if(hints != null) {
                for (HintRuleAppFinder hint : hints) {
                    RuleApplication ruleApp = followHint(hint, node, reasonNode);
                    if(ruleApp != null) {
                        if(!ruleApp.getProofNode().applicable(ruleApp, env)) {
                            Log.log(Log.DEBUG, ruleApp);
                            throw new StrategyException("The hint came up with an illegal rule application");
                        } else {
                            return ruleApp;
                        }
                    }
                }
            }
            reasonNode = reasonNode.getParent();
        }

        return null;
    }

    /*
     * ask the rule finder to find a rule. Errors are handled according to
     * settings.
     */
    private @Nullable RuleApplication followHint(HintRuleAppFinder hint,
            ProofNode node,
            ProofNode reasonNode) throws StrategyException {
        try {
            return hint.findRuleApplication(node, reasonNode);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation ...
     * <ol>
     * <li>Checks that the rule is applied on a literal program term
     * <li>Parses hints in the statements annotations.
     * <li>Adds the according hint application finders to the branches of the
     * node which are listed in the {@link RuleTagConstants#HINTS_ON_BRANCHES}
     * annotiation
     * </ol>
     */
    @Override
    public void notifyRuleApplication(RuleApplication ruleApp) throws StrategyException {

        try {
            Rule rule = ruleApp.getRule();
            ProofNode node = ruleApp.getProofNode();
            String hintOn = rule.getProperty(RuleTagConstants.HINTS_ON_BRANCHES);
            if(hintOn == null) {
                return;
            }

            TermSelector findSel = ruleApp.getFindSelector();
            Term findTerm = findSel.selectSubterm(node.getSequent());

            // find may have an update ... strip that away ...
            if (findTerm instanceof UpdateTerm) {
                findTerm = findTerm.getSubterm(0);
            }

            if (findTerm instanceof LiteralProgramTerm) {
                LiteralProgramTerm lpt = (LiteralProgramTerm) findTerm;
                String annotation = lpt.getProgram().getTextAnnotation(lpt.getProgramIndex());
                if(annotation == null) {
                    return;
                }
                List<HintRuleAppFinder> hints = hintParser.parse(annotation);
                for (String string : hintOn.split(" *, *")) {
                    int branchNo = Integer.parseInt(string);
                    ProofNode branch = node.getChildren().get(branchNo);
                    hintMap.put(branch, hints);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    };

    /**
     * Handle occurring exceptions.
     *
     * According to the flag {@link #raiseErrors}, exceptions are either wrapped
     * into a {@link StrategyException} or logged and ignored.
     *
     * @param e
     *            the exception to handle
     * @throws StrategyException
     *             if {@link #raiseErrors} is <code>true</code>
     */
    private void handleException(@NonNull Exception e) throws StrategyException {
        if(raiseErrors) {
            throw new StrategyException("Error while handling proof hints: " + e.getMessage(), e);
        } else {
            Log.log(Log.ERROR, "Error while handling proof hints");
            Log.stacktrace(Log.ERROR, e);
        }
    }

    @Override
    public String toString() {
        return "Hint Strategy";
    }

    /**
     * Get the value of {@link #raiseErrors}.
     * For the {@link de.uka.iti.pseudo.gui.parameters.ParameterSheet}.
     *
     * @return raiseErrors
     */
    public boolean getRaiseErrors() {
        return raiseErrors;
    }

    /**
     * Set the value of {@link #raiseErrors}.
     * For the {@link de.uka.iti.pseudo.gui.parameters.ParameterSheet}.
     *
     * @param raiseErrors the value to set
     */
    public void setRaiseErrors(boolean raiseErrors) {
        this.raiseErrors = raiseErrors;
    }

}