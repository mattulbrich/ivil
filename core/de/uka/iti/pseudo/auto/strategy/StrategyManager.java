package de.uka.iti.pseudo.auto.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;


// TODO DOC DOC!
public class StrategyManager {
    
    private Map<Class<? extends Strategy>, Strategy> registeredStrategies =
        new HashMap<Class<? extends Strategy>, Strategy>();
    
    private Strategy selectedStrategy; 
    private Proof proof;
    private Environment env;
    
    public StrategyManager(@NonNull Proof proof, @NonNull Environment env) {
        super();
        this.proof = proof;
        this.env = env;
    }

    public void registerStrategy(Class<? extends Strategy> clss) throws StrategyException {
        if(registeredStrategies.containsKey(clss))
            throw new StrategyException("Class " + clss + " has already been registered");
        
        try {
            Strategy newInstance = clss.newInstance();
            registerInternally(clss, newInstance);
        } catch (Exception e) {
            throw new StrategyException("Cannot instantiate class " + clss, e);
        } 
    }

    private void registerInternally(Class<? extends Strategy> clss, Strategy strategy) throws StrategyException {
        strategy.init(proof, env, this);
        registeredStrategies.put(clss, strategy);
        if(selectedStrategy == null)
            selectedStrategy = strategy;
    }
    
    public void registerAllKnownStrategies() throws StrategyException {
        for(Strategy strategy : ServiceLoader.load(Strategy.class)) {
            Class<? extends Strategy> clss = strategy.getClass();
            if(registeredStrategies.containsKey(clss))
                throw new StrategyException("Class " + clss + " has already been registered");
            registerInternally(clss, strategy);
        }
    }
    
    @SuppressWarnings("unchecked") 
    public <T extends Strategy> T getStrategy(Class<T> clss) {
        return (T) registeredStrategies.get(clss);
    }
    
    public Collection<Strategy> getAllStrategies() {
        return registeredStrategies.values();
    }

    public Strategy getSelectedStrategy() throws NoSuchElementException {
        return selectedStrategy;
    }

    public void setSelectedStrategy(Strategy selectedStrategy) {
        assert registeredStrategies.containsValue(selectedStrategy);
        this.selectedStrategy = selectedStrategy;
    }
    
}
