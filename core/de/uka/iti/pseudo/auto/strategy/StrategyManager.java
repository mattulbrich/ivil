package de.uka.iti.pseudo.auto.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class StrategyManager {
    
    private Map<Class<? extends Strategy>, Strategy> registeredStrategies =
        new HashMap<Class<? extends Strategy>, Strategy>();
    
    private Strategy selectedStrategy; 
    
    public void registerStrategy(Class<? extends Strategy> clss) throws StrategyException {
        if(registeredStrategies.containsKey(clss))
            throw new StrategyException("Class " + clss + " has already been registered");
        
        try {
            Strategy newInstance = clss.newInstance();
            registeredStrategies.put(clss, newInstance);
        } catch (Exception e) {
            throw new StrategyException("Cannot instantiate class " + clss, e);
        } 
    }
    
    public void registerAllKnownStrategies() throws StrategyException {
        for(Strategy strategy : ServiceLoader.load(Strategy.class)) {
            Class<? extends Strategy> clss = strategy.getClass();
            if(registeredStrategies.containsKey(clss))
                throw new StrategyException("Class " + clss + " has already been registered");
            registeredStrategies.put(clss, strategy);
        }
    }
    
    @SuppressWarnings("unchecked") 
    public <T extends Strategy> T getStrategy(Class<T> clss) {
        return (T) registeredStrategies.get(clss);
    }
    
    public Collection<Strategy> getAllStrategies() {
        return registeredStrategies.values();
    }

    public Strategy getSelectedStrategy() {
        return selectedStrategy;
    }

    public void setSelectedStrategy(Strategy selectedStrategy) {
        assert registeredStrategies.containsValue(selectedStrategy);
        this.selectedStrategy = selectedStrategy;
    }
    
}
