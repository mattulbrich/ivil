package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

import de.uka.iti.pseudo.auto.strategy.MyStrategy;
import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class AutoProofAction extends BarAction 
    implements Runnable, PropertyChangeListener, InitialisingAction {

    private static Icon goIcon = BarManager.makeIcon(AutoProofAction.class.getResource("img/cog_go.png"));
    private static Icon stopIcon = BarManager.makeIcon(AutoProofAction.class.getResource("img/cog_stop.png"));
    
    private Strategy strategy;
    
    private Thread thread = null;
    private boolean shouldStop;

    public AutoProofAction() {
        super("Automatic Proof", goIcon);
        putValue(SHORT_DESCRIPTION, "Run automatic proving on the current node");
    }
    
    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
    }
    
    public void actionPerformed(ActionEvent e) {

        // TODO synchronization!
        
        if(thread == null) {
            thread = new Thread(this, "Autoproving");
            getProofCenter().getMainWindow().firePropertyChange(MainWindow.IN_PROOF, true);
            shouldStop = false;
            thread.start();
        } else {
            shouldStop = true;
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
            getProofCenter().getMainWindow().firePropertyChange(MainWindow.IN_PROOF, false);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setIcon(((Boolean)evt.getNewValue()) ? stopIcon : goIcon);
    }

}
