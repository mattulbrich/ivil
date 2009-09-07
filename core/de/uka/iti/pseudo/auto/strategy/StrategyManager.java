/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;

/**
 * A StrategyManager is essentially a collection of all applicable strategies
 * for a proof context.
 * 
 * It has got one distinguished selected strategy which can be set and
 * retrieved.
 * 
 * New Strategies can be registered by their class. There can only be one
 * strategy per implementation (per manager).
 */
public class StrategyManager {

    /**
     * The registered strategies as map from their implementing class to the
     * actual strategy.
     */
    private Map<Class<? extends Strategy>, Strategy> registeredStrategies = new HashMap<Class<? extends Strategy>, Strategy>();

    /**
     * The currently selected strategy. This is not null as soon as the first
     * strategy has been registered.
     */
    private Strategy selectedStrategy;

    /**
     * The proof to which this manager is bound.
     */
    private Proof proof;

    /**
     * The environment of the proof to this manager.
     */
    private Environment env;

    /**
     * Instantiates a new strategy manager.
     * 
     * @param proof
     *            the proof to bind to
     * @param env
     *            the environment to bind to
     */
    public StrategyManager(@NonNull Proof proof, @NonNull Environment env) {
        super();
        this.proof = proof;
        this.env = env;
    }

    /**
     * Register a new strategy by its class.
     * 
     * The class must not yet have been registered. A new instance of the class
     * is created, initialised and added to the repository. If it is the first
     * class to be registered, it is automatically set to be the selected
     * strategy.
     * 
     * @param clss
     *            the strategy class to register
     * 
     * @throws StrategyException
     *             if the class is already registered or if the initialisation
     *             of the strategy object fails
     */
    public void registerStrategy(Class<? extends Strategy> clss)
            throws StrategyException {
        if (registeredStrategies.containsKey(clss))
            throw new StrategyException("Class " + clss
                    + " has already been registered");

        try {
            Strategy newInstance = clss.newInstance();
            registerInternally(newInstance);
        } catch (Exception e) {
            throw new StrategyException("Cannot instantiate class " + clss, e);
        }
    }

    /**
     * Do internal registration stuff:
     * <ol>
     * <li>initialise the strategy
     * <li>add it to the mapping of registered strategies
     * <li>make it the selected strategy if this is the first strategy to be
     * around
     * </ol>
     * 
     * @throws StrategyException
     *             possibly thrown by
     *             {@link Strategy#init(Proof, Environment, StrategyManager)}
     */
    private void registerInternally(Strategy strategy) throws StrategyException {
        strategy.init(proof, env, this);
        registeredStrategies.put(strategy.getClass(), strategy);
        if (selectedStrategy == null)
            selectedStrategy = strategy;
    }

    /**
     * Register all strategies which are known to the {@link ServiceLoader}
     * mechanism.
     * 
     * The classes which are listed in a text file (see {@link ServiceLoader}
     * for details) are registered. For each of them a new instance is created
     * and added to the system. If no class has been registered yet, the first
     * newly created object becomes the selected stratey.
     * 
     * @throws StrategyException
     *             if a class has already been registered or if the
     *             initialisation process fails for a strategy.
     */
    public void registerAllKnownStrategies() throws StrategyException {
        for (Strategy strategy : ServiceLoader.load(Strategy.class)) {
            Class<? extends Strategy> clss = strategy.getClass();
            if (registeredStrategies.containsKey(clss))
                throw new StrategyException("Class " + clss
                        + " has already been registered");
            registerInternally(strategy);
        }
    }

    /**
     * Gets a strategy by its implementing class. The class must be registered
     * for this not to return null.
     * 
     * @param clss
     *            the implementing class for which the strategy is to be
     *            returned
     * 
     * @return the strategy, or null if the class is not registered
     */
    @SuppressWarnings("unchecked") public <T extends Strategy> T getStrategy(
            Class<T> clss) {
        return (T) registeredStrategies.get(clss);
    }

    /**
     * Gets all strategies registered in this manager
     * 
     * @return an immutable collection of strategies.
     */
    public Collection<Strategy> getAllStrategies() {
        return registeredStrategies.values();
    }

    /**
     * Gets the currently selected strategy.
     * 
     * As soon as one class has been registered, this does not return null
     * 
     * @return the selected strategy, null if no class has been registered yet.
     */
    public Strategy getSelectedStrategy() {
        return selectedStrategy;
    }

    /**
     * Sets the currently selected strategy. The strategy must be an object
     * which is controlled by this manager.
     * 
     * @param selectedStrategy
     *            the new selected strategy
     */
    public void setSelectedStrategy(@NonNull Strategy selectedStrategy) {
        assert registeredStrategies.containsValue(selectedStrategy);
        this.selectedStrategy = selectedStrategy;
    }

}
