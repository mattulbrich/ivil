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
import java.util.Set;

import checkers.nullness.quals.LazyNonNull;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

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
     * in this set store the proof nodes for which this strategy was not able to
     * find a rule application. Later calls will then not search again.
     * 
     * TODO Is this caching sensible at all?
     */
    private Set<ProofNode> notMatching = new HashSet<ProofNode>();

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
     */
    @Override
    public @Nullable RuleApplication findRuleApplication()
            throws StrategyException {
        List<ProofNode> openGoals = proof.getOpenGoals();

        for (ProofNode goal : openGoals) {
            if (!notMatching.contains(goal)) {
                RuleApplication ra = findRuleApplication(goal);
                if (ra != null) {
                    return ra;
                } else {
                    notMatching.add(goal);
                }
            }
        }

        return null;
    }

    @Override
    public void notifyRuleApplication(RuleApplication ruleApp)
            throws StrategyException {
        // nothing done in this class. Subclasses may choose to do things
    }

    /**
     * Gets the proof associated with this strategy.
     * 
     * @return the proof associated with this strategy.
     */
    public final Proof getProof() {
        return proof;
    }

}
