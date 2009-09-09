package de.uka.iti.pseudo.auto.strategy;

import java.io.File;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFilter;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;

public class BreakpointStrategy implements Strategy, RuleApplicationFilter {
    
    private BreakpointManager breakPointManager = new BreakpointManager();
    
    /**
     * The set of rules which we do consult
     */
    private static final String REWRITE_CATEGORY = "symbex";
    
    /*
     * The configurable properties
     */
    private boolean obeyProgramBreakpoints = true;
    private boolean obeySourceBreakpoints = true;
    private boolean stopAtSkip = false;

    private Proof proof;

    private RewriteRuleCollection ruleCollection;
    
    @Override 
    public void init(Proof proof, Environment env, StrategyManager strategyManager)
            throws StrategyException {
        this.proof = proof;
        
        try {
            ruleCollection = new RewriteRuleCollection(env.getAllRules(), REWRITE_CATEGORY, env);
            ruleCollection.setApplicationFilter(this);
        } catch (RuleException e) {
            throw new StrategyException("Cannot initialise BreakpointStrategy", e);
        }
    }

    @Override 
    public RuleApplication findRuleApplication() {
        int countGoals = proof.getOpenGoals().size();
        
        for (int i = 0; i < countGoals; i++) {
            RuleApplication ra = ruleCollection.findRuleApplication(proof, i);
            if (ra != null) {
                return ra;
            }
        }
        
        return null;
    }
    
    private boolean hasBreakpoint(LiteralProgramTerm progTerm) {
        //
        // check for stop at skip
        if(stopAtSkip) {
            Statement s = progTerm.getStatement();
            if(s instanceof SkipStatement)
                return true;
        }
        
        Program program = progTerm.getProgram();
        int number = progTerm.getProgramIndex();

        //
        // check for program breakpoint
        if(obeyProgramBreakpoints) {
            if(breakPointManager.hasBreakpoint(program, number))
                return true;
        }

        //
        // check for source breakpoint:
        
        // we store in differs whether the statement in front of the current 
        // has a different source line number. only then the breakpoint is hit.
        File sourceFile = program.getSourceFile();
        if(sourceFile != null && obeySourceBreakpoints) {
            int sourceline = progTerm.getStatement().getSourceLineNumber();
            boolean differs = number == 0
                    || program.getStatement(number - 1).getSourceLineNumber() != sourceline;
            
            if(differs && breakPointManager.hasBreakpoint(sourceFile, sourceline))
                return true;
        }
        
        return false;
    }

    public BreakpointManager getBreakpointManager() {
        return breakPointManager;   
    }
   
    @Override 
    public String toString() {
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
    
    // due to ParameterSheet, we need get instead of is
    public boolean getStopAtSkip() {
        return stopAtSkip;
    }

    public void setStopAtSkip(boolean stopAtSkip) {
        this.stopAtSkip = stopAtSkip;
    }

    @Override 
    public boolean accepts(RuleApplication ruleApp) throws RuleException {
        int goal = ruleApp.getGoalNumber();
        Sequent sequent = proof.getGoal(goal).getSequent();
        Term find;
        try {
            find = ruleApp.getFindSelector().selectSubterm(sequent);
        } catch (ProofException e) {
            throw new RuleException(e);
        }
        
        if (!(find instanceof LiteralProgramTerm)) {
            find = find.getSubterm(0);
        }
        
        if (find instanceof LiteralProgramTerm) {
            LiteralProgramTerm progTerm = (LiteralProgramTerm) find;
            if (hasBreakpoint(progTerm))
                return false;
        } else {
            throw new RuleException(
                    "Rules in 'symbex' MUST match a program term or updated program terms, this rule did not: "
                            + ruleApp.getRule().getName());
        }
        
        return true;
    }
    
}
