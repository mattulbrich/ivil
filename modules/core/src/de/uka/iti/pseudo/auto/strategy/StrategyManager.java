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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * A StrategyManager is essentially a collection of all applicable strategies
 * for a proof context.
 * 
 * It has got one distinguished selected strategy which can be set and
 * retrieved. The initially selected strategy is retrieved by querying
 * {@link TestSettings} for the key {@value StrategyManager#DEFAULT_STRATEGY_KEY}.
 * 
 * New Strategies can be registered by their class. There can only be one
 * strategy per implementation (per manager).
 */
public class StrategyManager {
    
    /**
     * query the settings for this key to determine the first selected
     * strategy.
     */
    private static final String DEFAULT_STRATEGY_KEY = "pseudo.auto.defaultStrategy";
    
    private static final String DEFAULT_STRATEGY_CLASSNAME = 
        Settings.getInstance().getProperty(DEFAULT_STRATEGY_KEY, "");
    
    /**
     * The registered strategies as map from their implementing class to the
     * actual strategy.
     */
    private Map<Class<? extends Strategy>, Strategy> registeredStrategies = 
        new LinkedHashMap<Class<? extends Strategy>, Strategy>();

    /**
     * The currently selected strategy. This is not null as long the
     * pre-defined selected strategy is not registered.
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
        if (registeredStrategies.containsKey(clss)) {
            throw new StrategyException("Class " + clss
                    + " has already been registered");
        }
        
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
     * <li>make it the selected strategy if this is class to be selected first
     * and nothing else has been selected yet.
     * </ol>
     * 
     * @throws StrategyException
     *             possibly thrown by
     *             {@link Strategy#init(Proof, Environment, StrategyManager)}
     */
    private void registerInternally(Strategy strategy) throws StrategyException {
        strategy.init(proof, env, this);
        registeredStrategies.put(strategy.getClass(), strategy);
        if (selectedStrategy == null && 
                strategy.getClass().getName().equals(DEFAULT_STRATEGY_CLASSNAME)) {
            selectedStrategy = strategy;
        }
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
     * @throws StrategyException
     *             if the argument has not been registered previously.
     * 
     * @return the strategy
     */
    @SuppressWarnings("unchecked")
    public <T extends  Strategy> T getStrategy(Class<T> clss) throws StrategyException {
        Strategy strategy = registeredStrategies.get(clss);
        if(strategy == null)
            throw new StrategyException("Unregistered strategy " + clss);
        return (T) strategy;
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
