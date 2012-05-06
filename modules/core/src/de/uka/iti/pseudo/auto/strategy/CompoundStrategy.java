/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

/**
 * CompoundStrategy allows to combine different strategies.
 * 
 * To select find a rule application, one strategy after the other is asked. The
 * strategies to be considered are stored in an array ({@link #strategies})
 * which can be configured (e.g. by the UI)
 */
public class CompoundStrategy extends AbstractStrategy {
    
    private static final String STRATEGY_PACKAGE_PREFIX = "de.uka.iti.pseudo.auto.strategy.";

    /**
     * The {@link #strategies} are initialised to an array
     * of instanced of the here mentioned classes 
     */
    private static final Class<?>[] ORIGINAL_STRATEGIES = {
            SimplificationStrategy.class, BreakpointStrategy.class, SMTStrategy.class };

    /**
     * The strategy manager is needed to query all possible instances for
     * {@link #strategies}.
     */
    private StrategyManager strategyManager;
    
    /**
     * The array of applied strategies. In order of application.
     */
    private Strategy strategies[];
    
    /**
     * If all my strategies are {@link AbstractStrategy}s themselves,
     * we can apply better algorithms. 
     */
    // TODO Is this still needed?
//    private boolean allAbstractStrategy;

    /**
     * To find an application, query one strategy after the other.
     * 
     * If all children are {@link AbstractStrategy} themselves,
     * we can rely on the no-matching remembering of the superclass.
     * 
     * @return the first rule application found, or null if no strategy returns
     *         an application.
     */
//    public RuleApplication findRuleApplication()
//            throws StrategyException {
//        
//        if(allAbstractStrategy) {
//            return super.findRuleApplication();
//        } else {
//            for (Strategy strategy : strategies) {
//                RuleApplication ra = strategy.findRuleApplication();
//                if(ra != null)
//                    return ra;
//            }
//            return null;
//        }
//    }

    /**
     * Initialise the strategy. Create the initial {@link #strategies}. 
     */
    @Override
    @SuppressWarnings("unchecked")
    public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {
        super.init(proof, env, strategyManager);
        this.strategyManager = strategyManager;

        // look for strategy list in env
        String desiredStrategies = env.getProperty(
                this.getClass().getSimpleName() + ".strategies");

        if(desiredStrategies == null) {
            // no property set in the environment
            strategies = new Strategy[ORIGINAL_STRATEGIES.length];
            for (int i = 0; i < strategies.length; i++) {
                strategies[i] = strategyManager.getStrategy(
                        (Class<? extends Strategy>)ORIGINAL_STRATEGIES[i]);
            }

        } else {
            // read the property
            String[] s = desiredStrategies.trim().split(",\\s*");
            strategies = new Strategy[s.length];
            for (int i = 0; i < strategies.length; i++) {
                try {
                    strategies[i] = strategyManager.getStrategy(
                            (Class<? extends Strategy>)Class.forName(s[i]));
                } catch (ClassNotFoundException ex) {
                    try {
                        // test for short names of built-in strategies
                        strategies[i] = strategyManager.getStrategy((
                                Class<? extends Strategy>)Class.forName(STRATEGY_PACKAGE_PREFIX + s[i]));
                    } catch (ClassNotFoundException e) {
                        Log.log(Log.WARNING, s[i] + " does not name a proper class");
                        Log.stacktrace(e);
                        throw new StrategyException(s[i] + " does not name a strategy class", e);
                    }
                }
            }
        }

        assert strategiesError() == null : strategiesError();
    }

    /**
     * check for error in the strategies array.
     * <ol>
     * <li>No null reference may be stored
     * <li>All strategies must belong to the strategy manager
     * <li>No self reference may be stored
     * </ol>
     * 
     * @return null if no problem, a problem description otherwise
     */
    private @Nullable String strategiesError() {
        for (Strategy strategy : strategies) {
            if(strategy == null) {
                return "A strategy is null";
            }

            Collection<Strategy> allStrategies = strategyManager.getAllStrategies();
            if(!allStrategies.contains(strategy)) {
                return "A strategy is not known to the strategy manager";
            }

            if(strategy == this) {
                return "A compound strategy must not contain itself";
            }
        }

        return null;
    }

    /**
     * Gets the currently set strategies as immutable list.
     * 
     * @return an immutable list containing the currently set strategies.
     */
    public List<Strategy> getStrategies() {
        return Util.readOnlyArrayList(strategies);
    }

    /**
     * Sets the strategies. The given list is cloned to an array.
     * 
     * @param strategies
     *            the new strategies
     * @throws StrategyException
     *             if the argument cannot be installed
     */
    public void setStrategies(List<Strategy> strategies) throws StrategyException {
        this.strategies = new Strategy[strategies.size()];
        strategies.toArray(this.strategies);

        String error = strategiesError();
        if(error != null) {
            throw new StrategyException(error);
        }
    }

    /**
     * The name of this strategy
     */
    @Override 
    public String toString() {
        return "Compound Strategy";
    }

    /**
     * get a list of all possible strategies. Including those that have already
     * been used, but excluding "this".
     * 
     * This is actually used to leak the information to the UI which allows to
     * add new strategies. The call is delegated to the strategy manager.
     * 
     * @return the strategies known to the strategy manager.
     */
    public Collection<Strategy> getAllStrategies() {
        ArrayList<Strategy> all = new ArrayList<Strategy>(strategyManager.getAllStrategies());
        all.remove(this);
        return all;
    }
    
    /**
     * {@inheritDoc}
     * 
     * This method call is delegated to all inscribed strategies.
     */
    @Override public void beginSearch() throws StrategyException {
        super.beginSearch();
        for (Strategy strategy : getStrategies()) {
            strategy.beginSearch();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * This method call is delegated to all inscribed strategies.
     */
    @Override public void endSearch() {
        super.endSearch();
        for (Strategy strategy : getStrategies()) {
            strategy.endSearch();
        }
    }

    /**
     * delegates the search to all child strategies in order and returns the
     * first hit.
     */
    @Override 
    public @Nullable RuleApplication findRuleApplication(ProofNode target)
            throws StrategyException {
        
        for (Strategy strategy : strategies) {
            RuleApplication ra = strategy.findRuleApplication(target);
            if(ra != null) {
                return ra;
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The implementation of compound strategy delegates the method call to all
     * child strategies.
     */
    @Override
    public void notifyRuleApplication(RuleApplication ruleApp)
            throws StrategyException {
        for (Strategy strategy : strategies) {
            strategy.notifyRuleApplication(ruleApp);
        }
    }

}
