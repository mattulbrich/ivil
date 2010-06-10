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
package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class AutoProofAction extends BarAction 
    implements Runnable, PropertyChangeListener, InitialisingAction {

    private static Icon goIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_go.png"));
    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_stop.png"));
    
    private Thread thread = null;
    private boolean shouldStop;

    public AutoProofAction() {
        super("Automatic Proof", goIcon);
        putValue(SHORT_DESCRIPTION, "Run automatic proving on the current node");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
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
        Proof proof = getProofCenter().getProof();
        Strategy strategy = getProofCenter().getStrategyManager().getSelectedStrategy();
        
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
                    ProofNode currentNode = getProofCenter().getCurrentProofNode();
                    if(currentNode == null || currentNode.getChildren() != null) {
                        ProofNode first = proof.getGoal(0);
                        getProofCenter().fireSelectedProofNode(first);
                    }
                    // endSearch is called in finally
                    return;
                }

                try {
                    getProofCenter().apply(ruleAppl);
                    strategy.notifyRuleApplication(ruleAppl);
                } catch (ProofException e) {
                    System.err.println("Error while applying rule " + ruleAppl.getRule().getName() + 
                            " on " + ruleAppl.getFindSelector() + " on goal " +
                            ruleAppl.getGoalNumber());
                    throw e;
                }
            }
            
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), e);
        } finally {
            strategy.endSearch();
            thread = null;
            proof.getLock().unlock();
            getProofCenter().firePropertyChange(ProofCenter.ONGOING_PROOF, false);
            // some listeners have been switched off, they might want to update now.
            proof.notifyObservers();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setIcon(((Boolean)evt.getNewValue()) ? stopIcon : goIcon);
    }

}
