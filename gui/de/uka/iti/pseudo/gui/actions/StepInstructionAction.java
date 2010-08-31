package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

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
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// FIXME ... this is alpha version ...
// Find a unified framework for automated rule application
// @see AutoProofAction

/**
 * if the currently selected proof node is an open goal and has a unique line
 * number, the currently active strategy will be applied until all childrean are
 * either closed or have another unique line number.
 */
public class StepInstructionAction extends BarAction implements
        PropertyChangeListener, InitialisingAction {
    
    private static final long serialVersionUID = 5535387689071989365L;
    
    private ProofNode selectedProofNode;


    public StepInstructionAction() {
        super("Step Instruction", GUIUtil.makeIcon(LoadProblemAction.class.getResource("img/control_play.png")));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        putValue(SHORT_DESCRIPTION, "symbolically execute a single intermediate code instruction");
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // has no effect on nodes with children
        if (null != selectedProofNode.getChildren())
            return; // if this effect is undesired, select the first open goal
                    // that has a line number

        int initialLine = selectedProofNode.getProgramLineNumber();

        // you cannot step for a line, if you can't identify your line number
        if (initialLine < 0)
            return;

        // do this in a thread of its own?
        ProofCenter pc = getProofCenter();
        // TODO use a compound strategy to ensure that symbolic execution rules can be dealt with
        Strategy strategy = pc.getStrategyManager().getSelectedStrategy();
        Proof proof = pc.getProof();

        List<ProofNode> todo = new LinkedList<ProofNode>();
        todo.add(selectedProofNode);

        if (!proof.getLock().tryLock()) {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Proof locked by another thread");
            return;
        }
        
        try {
            strategy.init(proof, pc.getEnvironment(), pc.getStrategyManager());
            strategy.beginSearch();
            
            ProofNode current = null;

            while (!todo.isEmpty()) {
                current = todo.remove(0);

                RuleApplication ra = strategy.findRuleApplication(current);

                if (ra != null) {
                    proof.apply(ra, pc.getEnvironment());
                    strategy.notifyRuleApplication(ra);

                    for (ProofNode node : current.getChildren()) {
                        int ln = node.getProgramLineNumber();
                        if (-1 == ln || ln == initialLine) {
                            todo.add(node);
                        }
                    }
                } else
                    ExceptionDialog
                            .showExceptionDialog(getParentFrame(),
                                    "The currently selected proof strategy is to weak to do another step");
            }

            if (selectedProofNode.isClosed()) {
                if (proof.hasOpenGoals())
                    pc.fireSelectedProofNode(proof.getGoal(0));
                else
                    pc.fireSelectedProofNode(proof.getRoot());
            } else {
                // find first unclosed node
                current = selectedProofNode;
                while (current.getChildren() != null)
                    for (ProofNode child : current.getChildren())
                        if (!child.isClosed())
                            current = child;

                pc.fireSelectedProofNode(current);
            }

        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        } finally {
            strategy.endSearch();
            proof.getLock().unlock();
            // some listeners have been switched off, they might want to update
            // now.
            proof.notifyObservers();
        }
        
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName()))
            selectedProofNode = (ProofNode) evt.getNewValue();
    }

    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(
                ProofCenter.SELECTED_PROOFNODE, this);
        selectedProofNode = getProofCenter().getProof().getRoot();
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
            throw new StrategyException(
                    "Cannot initialise StepInstructionStrategy", e);
        }
    }

    @Override 
    public RuleApplication findRuleApplication(int goalNumber) {
        RuleApplication ra = ruleCollection.findRuleApplication(getProof(), goalNumber);
        return ra;
    }
}
