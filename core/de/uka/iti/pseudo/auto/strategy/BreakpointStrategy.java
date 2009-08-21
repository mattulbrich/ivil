package de.uka.iti.pseudo.auto.strategy;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;

public class BreakpointStrategy implements Strategy {
    
    private BreakpointManager breakPointManager = new BreakpointManager();
    private boolean obeyProgramBreakpoints = true;
    private boolean obeySourceBreakpoints = true;

    @Override 
    public RuleApplication findRuleApplication(Proof proof)
            throws StrategyException {
        // TODO Implement BreakpointStrategy.findRuleApplication
        return null;
    }
    
    public BreakpointManager getBreakpointManager() {
        return breakPointManager;   
    }

    @Override 
    public void init(Environment env, StrategyManager strategyManager)
            throws StrategyException {
        
    }
    
    @Override public String toString() {
        return "Breakpoint Strategy";
    }

    //
    // getter and setter
    //
    
    
    // due to ParameterSheet, we need get instead of is
    public boolean getObeyProgramBreakpoints() {
        return obeyProgramBreakpoints;
    }

    public void setObeyProgramBreakpoints(boolean obeyProgramBreakpoints) {
        this.obeyProgramBreakpoints = obeyProgramBreakpoints;
    }

    // due to ParameterSheet, we need get instead of is
    public boolean getObeySourceBreakpoints() {
        return obeySourceBreakpoints;
    }

    public void setObeySourceBreakpoints(boolean obeySourceBreakpoints) {
        this.obeySourceBreakpoints = obeySourceBreakpoints;
    }

}
