package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import de.uka.iti.pseudo.auto.MyStrategy;
import de.uka.iti.pseudo.auto.Strategy;
import de.uka.iti.pseudo.gui.StateConstants;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class AutoProofAction extends AbstractStateListeningAction implements Runnable {

    private static Icon goIcon = BarManager.makeIcon(AutoProofAction.class.getResource("img/cog_go.png"));
    private static Icon stopIcon = BarManager.makeIcon(AutoProofAction.class.getResource("img/cog_stop.png"));
    
    private Strategy strategy;
    
    private Thread thread = null;
    private boolean shouldStop;

    public AutoProofAction() {
        super("Automatic Proof", goIcon);
        putValue(SHORT_DESCRIPTION, "Cut the current proof at the selected node");
    }

    public void actionPerformed(ActionEvent e) {

        // TODO synchronization!
        
        if(thread == null) {
            thread = new Thread(this, "Autoproving");
            StateChangeEvent evt = new StateChangeEvent(this, StateConstants.IN_PROOF, true);
            getProofCenter().getBarManager().fireStateChange(evt);
            shouldStop = false;
            thread.start();
        } else {
            shouldStop = true;
        }
    }

    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateConstants.IN_PROOF)) {
            setIcon(e.isActive() ? stopIcon : goIcon);
        }
    }

    public void run() {
        try {
            if(strategy == null)
                strategy = new MyStrategy(getProofCenter().getEnvironment());

            while(true) {
                RuleApplication ruleAppl = strategy.findRuleApplication(getProofCenter().getProof());

                if(ruleAppl == null || shouldStop) {
                    return;
                }

                try {
                    getProofCenter().apply(ruleAppl);
                } catch (ProofException e) {
                    System.err.println("Error while applying rule " + ruleAppl.getRule().getName() + 
                            " on " + ruleAppl.getFindSelector() + " on goal " +
                            ruleAppl.getGoalNumber());
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            thread = null;
            StateChangeEvent evt = new StateChangeEvent(this, StateConstants.IN_PROOF, false);
            getProofCenter().getBarManager().fireStateChange(evt);
        }
    }

}
