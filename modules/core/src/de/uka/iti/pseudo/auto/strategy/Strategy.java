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

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

/**
 * A Strategy can for a given proof provide the system with a rule application
 * to apply next.
 *
 * The strategy is only provided with the proof itself. It can however store
 * additional information since there is only one strategy object per registered
 * class and per problem.
 *
 * Strategy objects are created using reflection and, therefore, implementing
 * classes need to provide a default constructor.
 *
 * The possible sequences of method calls on a strategy can be described using a
 * regular expression:
 * <pre>
 *  init
 *   (
 *     beginSearch
 *     ( findRuleApplication<sup>?</sup> notifyRuleApplication )<sup>*</sup>
 *     endSearch
 *   )<sup>*</sup>
 * </pre>
 *
 * It is ensured by locking that after a call to {@link #beginSearch()} the proof
 * is only changed by the current strategy. <b>Please note:</b> Since other
 * strategies may also be invoked (e.g. combined in a {@link CompoundStrategy}),
 * the strategy should be able to cope with unexpected changes. Every strategy is
 * notified about every application then, however.
 */
public interface Strategy {

    /**
     * The property to be used to mark {@link RuleApplication}s with the
     * strategy which found them.
     */
    String STRATEGY_PROPERTY = "ivil.strategy";

    /**
     * Find an applicable rule application. The proof upon which the strategy
     * has to work has been set using
     * {@link #init(Proof, Environment, StrategyManager), StrategyManager)} Find
     * a rule to apply for this proof.
     *
     * A strategy may return null to indicate that it cannot provide a rule
     * application.
     *
     * @return the rule application to apply or null
     *
     * @throws StrategyException
     *             if the strategy has run into difficulties.
     * @throws InterruptedException
     *             strategies may choose to check for interruption and signal
     *             that through this exception
     */
    @Nullable RuleApplication findRuleApplication() throws StrategyException, InterruptedException;

    /**
     * Find an applicable rule application. The proof upon which the strategy
     * has to work has been set using
     * {@link #init(Proof, Environment, StrategyManager), StrategyManager)}. Find
     * a rule to apply for this proof.
     *
     * A strategy may return null to indicate that it cannot provide a rule
     * application.
     *
     * Only rule applications for target <code>node</code> are considered. The
     * argument must be a node of the tree of the proof with which this strategy
     * has been initialised. It should not have set a rule application, yet
     * (hence, must be a leave in the proof tree).
     *
     * If the result is not <code>null</code>, the returned rule application is
     * guaranteed to point to the given proof node.
     *
     * @see #findRuleApplication()
     * @param node
     *            a proof node which is a leaf of the proof under consideration
     * @throws StrategyException
     *             if the strategy has run into difficulties.
     * @throws InterruptedException
     *             strategies may choose to check for interruption and signal
     *             that through this exception
     */
    @Nullable RuleApplication findRuleApplication(@NonNull ProofNode node)
            throws StrategyException, InterruptedException;

    /**
     * Initialise this strategy.
     *
     * Strategies are constructed using the default constructor. They need,
     * however, to be provided with information on the proof to operate on, the
     * environment to use and the strategy manager under which they operate.
     *
     * It is guaranteed that this method is called exactly once and prior to the
     * first call to {@link #findRuleApplication(Proof)}.
     *
     * @param proof
     *            the proof object upon which the strategy will operate in the
     *            future.
     * @param env
     *            the environment of the proof under consideration
     * @param strategyManager
     *            the strategy manager of the proof under consideration
     *
     * @throws StrategyException
     *             if the initialisation fails
     */
    void init(@NonNull Proof proof, @NonNull Environment env,
            @NonNull StrategyManager strategyManager) throws StrategyException;

    /**
     * Indicate the beginning of an automatic search using this strategy.
     *
     * This method is invoked on the strategy to indicate that - if necessary -
     * state dependent information should be recalculated.
     *
     * It is ensured that this method is called only after init has been called.
     * After a call to {@link #init(Proof, Environment, StrategyManager)}
     * or {@link #endSearch()} this method is called before
     * {@link #findRuleApplication()} is called again.
     *
     * @throws StrategyException
     *             if the strategy fails
     */
    void beginSearch() throws StrategyException;

    /**
     * Indicate that a rule application has been applied to the proof. The
     * notification is provided regardless of the strategy that came with it.
     * The notification happens <b>after</b> the application has already been
     * applied to the proof object.
     *
     * @param ruleApp
     *            the ruleApplication that has been applied to
     * @throws StrategyException
     */
    void notifyRuleApplication(@NonNull RuleApplication ruleApp)
            throws StrategyException;

    /**
     * Indicate the end of an automatic search using this strategy.
     *
     * This method is invoked on the strategy to indicate that - if necessary or
     * desired - state dependent information can be invalidated, possible
     * freeing memory.
     */
   void endSearch();

    /**
     * Provides a user readable name for this strategy.
     *
     * @return the name of this strategy
     */
    @Override @NonNull String toString();
}