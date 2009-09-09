/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.Util;

/**
 * CompoundStrategy allows to combine different strategies.
 * 
 * To select find a rule application, one strategy after the other is asked. The
 * strategies to be considered are stored in an array ({@link #strategies})
 * which can be configured (e.g. by the UI)
 */
public class CompoundStrategy implements Strategy {
    
    /**
     * The {@link #strategies} are initialised to an array
     * of instanced of the here mentioned classes 
     */
    private static final Class<?>[] ORIGINAL_STRATEGIES = {
            SimplificationStrategy.class, BreakpointStrategy.class };

    /**
     * The proof object (currently not needed)
     */
    private Proof proof;
    
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
     * To find an application, query one strategy after the other.
     * 
     * @return the first rule application found, or null if no strategy returns
     *         an application.
     */
    public RuleApplication findRuleApplication()
            throws StrategyException {
        
        for (Strategy strategy : strategies) {
            RuleApplication ra = strategy.findRuleApplication();
            if(ra != null)
                return ra;
        }
        return null;
    }

    /**
     * Initialise the strategy. Create the initial {@link #strategies}. 
     */
    public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {
        this.proof = proof;
        this.strategyManager = strategyManager;
        
        strategies = new Strategy[ORIGINAL_STRATEGIES.length];
        for (int i = 0; i < strategies.length; i++) {
            strategies[i] = strategyManager.getStrategy((Class<? extends Strategy>)ORIGINAL_STRATEGIES[i]);
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
    private String strategiesError() {
        for (Strategy strategy : strategies) {
            if(strategy == null)
                return "A strategy is null";
            Collection<Strategy> allStrategies = strategyManager.getAllStrategies();
            
            if(!allStrategies.contains(strategy))
                return "A strategy is not known to the strategy manager";
            
            if(strategy == this)
                return "A compound strategy may not contain itself";
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
     * Sets the strategies.
     * 
     * @param strategies
     *            the new strategies
     * @throws RuntimeException
     *             if the argument cannot be installed
     */
    public void setStrategies(List<Strategy> strategies) throws RuntimeException {
        this.strategies = new Strategy[strategies.size()];
        strategies.toArray(this.strategies);
        
        String error = strategiesError();
        if(error != null)
            throw new RuntimeException(error);
    }
    
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

}
