package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.auto.strategy.AbstractStrategy;
import de.uka.iti.pseudo.auto.strategy.BreakpointStrategy;
import de.uka.iti.pseudo.auto.strategy.RewriteRuleCollection;
import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// FIXME ... this is alpha version ...
// Find a unified framework for automated rule application
// @see AutoProofAction

public class StepInstructionAction extends BarAction implements InitialisingAction {
    
    Strategy symbexStrategy;


    public StepInstructionAction() {
        super("Step Instruction", GUIUtil.makeIcon(LoadProblemAction.class.getResource("img/control_play.png")));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        putValue(SHORT_DESCRIPTION, "symbolically execute a single intermediate code instruction");
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // do this in a thread of its own?
        StepInstructionStrategy strategy = new StepInstructionStrategy();
        ProofCenter pc = getProofCenter();
        Proof proof = pc.getProof();
        
        if (!proof.getLock().tryLock()) {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Proof locked by another thread");
            return;
        }
        
        try {
            strategy.init(proof, pc.getEnvironment(), pc.getStrategyManager());
            strategy.beginSearch();
            RuleApplication ra = strategy.findRuleApplication();
            
            if(ra != null) {
                proof.apply(ra, pc.getEnvironment());
            }
            
            pc.fireSelectedProofNode(proof.getGoal(0));
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        } finally {
            strategy.endSearch();
            proof.getLock().unlock();
        }
        
    }

    @Override
    public void initialised() {
        symbexStrategy = new BreakpointStrategy();
    }

}


class StepInstructionStrategy extends AbstractStrategy {
    
    /**
     * The set of rules which we do consult
     */
    private static final String REWRITE_CATEGORY = "symbex";
    private RewriteRuleCollection ruleCollection;
    
    
    @Override 
    public void init(Proof proof, Environment env, StrategyManager strategyManager)
            throws StrategyException {
        super.init(proof, env, strategyManager);
        try {
            ruleCollection = new RewriteRuleCollection(env.getAllRules(), REWRITE_CATEGORY, env);
        } catch (RuleException e) {
            throw new StrategyException("Cannot initialise BreakpointStrategy", e);
        }
    }

    @Override 
    public RuleApplication findRuleApplication(int goalNumber) {
        RuleApplication ra = ruleCollection.findRuleApplication(getProof(), goalNumber);
        return ra;
    }
}
