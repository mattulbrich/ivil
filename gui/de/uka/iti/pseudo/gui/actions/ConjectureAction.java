package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;


// TODO DOC!
@SuppressWarnings("serial")
public class ConjectureAction extends BarAction implements InitialisingAction, PropertyChangeListener {

    private Rule cutRule;
    private String conjecture;

    public ConjectureAction() {
        super("Add a Conjecture",
                GUIUtil.makeIcon(LoadProblemAction.class.getResource("img/lightbulb_add.png")));
        
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        putValue(SHORT_DESCRIPTION, "Add a hypothesis and prove it");
    }

    @Override
    public void initialised() {
        ProofCenter proofCenter = getProofCenter();

        cutRule = proofCenter.getEnvironment().getRule("cut");
        if(cutRule == null) {
            Log.log(Log.WARNING, "Rule 'cut' not found");
            setEnabled(false);
        } else {
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, this);
            proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        }
       
    }

    // TODO Do this on a task thread
    @Override
    public void actionPerformed(ActionEvent e) {
        
        conjecture = JOptionPane.showInputDialog("Conjecture");

        if(conjecture == null) {
            return;
        }
        
        // TODO synchronization!
        getProofCenter()
                    .firePropertyChange(ProofCenter.ONGOING_PROOF, true);

        run();
        
    }
        
    private void run() {
       

        final ProofCenter proofCenter = getProofCenter();
        Proof proof = proofCenter.getProof();
        ProofNode currentProofNode = proofCenter.getCurrentProofNode();
        Strategy strategy = proofCenter.getStrategyManager().getSelectedStrategy();
        
        if (!proof.getLock().tryLock()) {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Proof locked by another thread");
            return;
        }
        
        try {

            Term term = TermMaker.makeAndTypeTerm(conjecture, 
                    proofCenter.getEnvironment(), "user input");

            MutableRuleApplication ram = new MutableRuleApplication();
            ram.setRule(cutRule);
            ram.setProofNode(currentProofNode);
            ram.getSchemaVariableMapping().put("%inst", term);
            proofCenter.getProof().apply(ram, proofCenter.getEnvironment());

            Queue<ProofNode> todo = new LinkedList<ProofNode>(); 
            ProofNode topNode = currentProofNode.getChildren().get(1);
            todo.add(topNode);

            // init() is called upon creation of the strategy, and only once!
            // strategy.init(proof, pc.getEnvironment(), pc.getStrategyManager());
            strategy.beginSearch();

            ProofNode current = null;

            while (!todo.isEmpty()) {
                current = todo.remove();

                RuleApplication ra = strategy.findRuleApplication(current);

                if (ra != null) {
                    proofCenter.apply(ra);
                    strategy.notifyRuleApplication(ra);

                    for (ProofNode node : current.getChildren())
                        todo.add(node);
                } else if (current.getChildren() != null)
                    for (ProofNode node : current.getChildren())
                        todo.add(node);
            }
            
            ProofNode next = currentProofNode.getChildren().get(0);
            if (!topNode.isClosed()) {
                int result = JOptionPane.showConfirmDialog(getParentFrame(),
                        "The proof branch cannot be closed. Keep it?",
                        "Question", JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.NO_OPTION) {
                    proofCenter.prune(currentProofNode);
                    next = currentProofNode;
                } 
            }
            proofCenter.fireSelectedProofNode(next);
            
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        } finally {
            strategy.endSearch();
            proof.getLock().unlock();
         // TODO put this in the after-work part of a SwingWorker
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
                }});
            proofCenter.fireProoftreeChangedNotification(true);
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        ProofCenter proofCenter = getProofCenter();

        boolean ongoing = (Boolean) proofCenter.getProperty(ProofCenter.ONGOING_PROOF);
        ProofNode curProofNode = proofCenter.getCurrentProofNode();
        boolean isLeaf = curProofNode != null && curProofNode.getChildren() == null;

        setEnabled(!ongoing && isLeaf);
    }

}
