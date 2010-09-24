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
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

/**
 * Tries to automatically close all nodes below the selected one with the
 * current strategy. If an inner node is selected, all children will be used.
 * 
 * @see AutoProofAction
 * 
 * @author felden@ira.uka.de
 * 
 */
@SuppressWarnings("serial")
public class AutoProofSubtreeAction extends BarAction implements Runnable,
        PropertyChangeListener, InitialisingAction, NotificationListener {

    private static Icon goIcon = GUIUtil.makeIcon(AutoProofAction.class
            .getResource("img/cog_go.png"));
    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class
            .getResource("img/cog_stop.png"));

    private Thread thread = null;
    private boolean shouldStop;

    private ProofNode selectedProofNode;

    public AutoProofSubtreeAction() {
        super("Automatic Proof Subtree", goIcon);
        putValue(SHORT_DESCRIPTION, "Run automatic proving on the current node");
    }

    public void initialised() {
        getProofCenter().addPropertyChangeListener(
                ProofCenter.ONGOING_PROOF, this);
        getProofCenter().addPropertyChangeListener(
                ProofCenter.SELECTED_PROOFNODE, this);
        getProofCenter().addNotificationListener(
                ProofCenter.PROOFTREE_HAS_CHANGED, this);
        
        selectedProofNode = getProofCenter().getProof().getRoot();
    }

    public void actionPerformed(ActionEvent e) {

        // TODO synchronization!
        if (thread == null) {
            thread = new Thread(this, "Autoproving");
            getProofCenter()
                    .firePropertyChange(ProofCenter.ONGOING_PROOF, true);
            shouldStop = false;
            thread.start();
        } else {
            shouldStop = true;
        }
    }

    public void run() {
        Proof proof = getProofCenter().getProof();
        final ProofCenter pc = getProofCenter();
        Strategy strategy = pc.getStrategyManager().getSelectedStrategy();

        // if there are no open goals disable this action, as the proof must
        // have been closed
        if (!proof.hasOpenGoals()) {
            ExceptionDialog
                    .showExceptionDialog(getParentFrame(),
                            "Tried to proof an allready closed proof. This should not be allowed.");
            setEnabled(false);
            return;
        }

        List<ProofNode> todo = new LinkedList<ProofNode>();
        todo.add(selectedProofNode);

        if (!proof.getLock().tryLock()) {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Proof locked by another thread");
            return;
        }

        try {
            // init() is called upon creation of the strategy, and only once!
            // strategy.init(proof, pc.getEnvironment(), pc.getStrategyManager());
            strategy.beginSearch();

            ProofNode current = null;

            while (!todo.isEmpty() && !shouldStop) {
                current = todo.remove(0);

                RuleApplication ra = strategy.findRuleApplication(current);

                if (ra != null) {
                    ProofNode next = pc.apply(ra);
                    strategy.notifyRuleApplication(ra);
                    // pc.fireSelectedProofNode(next);

                    for (ProofNode node : current.getChildren())
                            todo.add(node);
                } else if (current.getChildren() != null)
                    for (ProofNode node : current.getChildren())
                        todo.add(node);
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
                    // some listeners have been switched off, they might want to update now.
                    pc.fireNotification(ProofCenter.PROOFTREE_HAS_CHANGED);
                }});
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            selectedProofNode = (ProofNode) evt.getNewValue();
            // TODO update enable state at this point.
        }
        
        // FIXME?! Really? For the embedded action you want to change the icon?
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            setIcon(((Boolean) evt.getNewValue()) ? stopIcon : goIcon);
        }
    }

    @Override
    public void handleNotification(NotificationEvent event) {
        // TODO ... is this what we want? Should depend on whether there are open goals
        // under the currently selected node.
        Proof proof = getProofCenter().getProof();
        setEnabled(proof.hasOpenGoals());
    }

}