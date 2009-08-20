package de.uka.iti.pseudo.auto.strategy;

import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;

public class BreakpointStrategy implements Strategy {
    
    BreakpointManager breakPointManager = new BreakpointManager();

    @Override public RuleApplication findRuleApplication(Proof proof)
            throws StrategyException {
        // TODO Implement BreakpointStrategy.findRuleApplication
        return null;
    }
    
    public BreakpointManager getBreakpointManager() {
        return breakPointManager;   
    }

}
