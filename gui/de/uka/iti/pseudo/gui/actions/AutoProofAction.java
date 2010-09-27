/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class AutoProofAction extends BarAction 
    implements Runnable, PropertyChangeListener, InitialisingAction, NotificationListener {

    private static Icon goIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_go.png"));
    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_stop.png"));
    
    private Thread thread = null;
    private boolean shouldStop;

    public AutoProofAction() {
        super("Automatic Proof", goIcon);
        putValue(SHORT_DESCRIPTION, "Run automatic proving on all nodes");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        getProofCenter().addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
    }
    
    public void actionPerformed(ActionEvent e) {

        // TODO synchronization!
        if(thread == null) {
            thread = new Thread(this, "Autoproving");
            getProofCenter().firePropertyChange(ProofCenter.ONGOING_PROOF, true);
            shouldStop = false;
            thread.start();
        } else {
            shouldStop = true;
        }
    }

    public void run() {
        final ProofCenter pc = getProofCenter();
        Proof proof = pc.getProof();
        Strategy strategy = pc.getStrategyManager().getSelectedStrategy();
        
        //if there are no open goals disable this action, as the proof must have been closed
        if(!proof.hasOpenGoals()) {
        	ExceptionDialog.showExceptionDialog(getParentFrame(),
            "Tried to proof an allready closed proof. This should not be allowed.");
        	setEnabled(false);
        	return;
        }
        
        if (!proof.getLock().tryLock()) {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Proof locked by another thread");
            return;
        }
        
        try {
            
            strategy.beginSearch();
            
            while(true) {
                RuleApplication ruleAppl = strategy.findRuleApplication();

                if(ruleAppl == null || shouldStop) {
                    // we should stop: select an open goal
                    ProofNode currentNode = pc.getCurrentProofNode();
                    List<ProofNode> openGoals = proof.getOpenGoals();
                    if(currentNode == null || currentNode.getChildren() != null) {
                        ProofNode selected;
                        // bugfix for bug #1001
                        if (openGoals.size() > 0) {
                            selected = openGoals.get(0);
                        } else {
                            selected = proof.getRoot();
                        }
                        pc.fireSelectedProofNode(selected);
                    }
                    // endSearch is called in finally
                    return;
                }

                try {
                    pc.apply(ruleAppl);
                    strategy.notifyRuleApplication(ruleAppl);
                } catch (ProofException e) {
                    Log.log(Log.ERROR, "Error while applying rule " + ruleAppl.getRule().getName() + 
                            " on " + ruleAppl.getFindSelector() + " on goal #" +
                            ruleAppl.getProofNode().getNumber());
                    throw e;
                }
            }
            
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), e);
        } finally {
            strategy.endSearch();
            thread = null;
            proof.getLock().unlock();
            // TODO put this in the after-work part of a SwingWorker
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    pc.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
                }});
            pc.fireProoftreeChangedNotification(true);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setIcon(((Boolean)evt.getNewValue()) ? stopIcon : goIcon);
    }

    @Override
    public void handleNotification(NotificationEvent evt) {
        if(evt.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED)) {
            Proof proof = getProofCenter().getProof();
            setEnabled(proof.hasOpenGoals());
        }
    }

}
