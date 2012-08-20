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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import nonnull.Nullable;
import checkers.nullness.quals.LazyNonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * This abstract class keeps a list of proof nodes to which this strategy cannot
 * be applied.
 *
 * It implements the findRuleApplication by querying only those proof nodes
 * which are not tagged as "not matching" using
 *
 */
public abstract class AbstractStrategy implements Strategy {

    /**
     * The setting can be triggered to mark every rule application found with
     * the strategy which found it.
     */
    private static final boolean MARK_RULES =
            Settings.getInstance().getBoolean("pseudo.markRuleApps", false);

    /**
     * in this set store the proof nodes for which this strategy was not able to
     * find a rule application. Later calls will then not search again.
     *
     * TODO Is this caching sensible at all?
     */
    private final Set<ProofNode> notMatching = new HashSet<ProofNode>();

    /**
     * The proof object to which the strategy belongs.
     */
    private @LazyNonNull Proof proof;

    @Override
    public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {
        this.proof = proof;
    }

    @Override
    public void beginSearch() throws StrategyException {
        // nothing done in this class. Subclasses may choose to do things
    }

    @Override
    public void endSearch() {
        // empty the cache.
        notMatching.clear();
    }

    /**
     * Find an applicable rule application.
     *
     * The proof upon which the strategy has to work has been set using
     * {@link #init(Proof, Environment, StrategyManager), StrategyManager)}.
     *
     * This implementation will call {@link #findRuleApplication(ProofNode)} on
     * all open goals and return the first hit. If this strategy cannot find a
     * rule application for a proof node, that fact will be cached ensuring that
     * the search is not conducted a second time.
     *
     * If no rule application can be found, <code>null</code> is returned.
     *
     * @return the rule application to apply or null
     *
     * @throws StrategyException
     *             if the strategy has run into difficulties.
     * @throws InterruptedException
     *             if the find algorithm has been interrupted
     */
    @Override
    public @Nullable RuleApplication findRuleApplication()
            throws StrategyException, InterruptedException {
        List<ProofNode> openGoals = proof.getOpenGoals();

        for (ProofNode goal : openGoals) {
            if (!notMatching.contains(goal)) {
                RuleApplication ra = findRuleApplication(goal);
                markRuleApp(ra);
                if (ra != null) {
                    return ra;
                } else {
                    notMatching.add(goal);
                }
            }
        }

        return null;
    }

    /**
     * set a property on the rule application to identify the strategy which has
     * found it. The method {@link #toString()} is used to create the mark
     * string. Mainly for debugging purposes.
     *
     * @param ra a modifyable rule application
     */
    protected void markRuleApp(@NonNull RuleApplication ra) {
        if(MARK_RULES) {
            Map<String, String> properties = ra.getProperties();
            if(!properties.containsKey(STRATEGY_PROPERTY)) {
                properties.put(STRATEGY_PROPERTY, this.toString());
            }
        }
    }

    @Override
    public void notifyRuleApplication(@NonNull RuleApplication ruleApp)
            throws StrategyException {
        // nothing done in this class. Subclasses may choose to do things
    }

    /**
     * Gets the proof associated with this strategy.
     *
     * @return the proof associated with this strategy.
     */
    public final @NonNull Proof getProof() {
        return proof;
    }

}
