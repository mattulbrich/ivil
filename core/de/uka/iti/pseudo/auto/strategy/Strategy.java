/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
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
 */
public interface Strategy {

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
     */
    @Nullable RuleApplication findRuleApplication() throws StrategyException;

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
     * Provides a user readable name for this strategy.
     * 
     * @return the name of this strategy
     */
    @NonNull String toString();

}