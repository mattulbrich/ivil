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

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofDaemon.Job;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * Tries to automatically close all nodes below the selected one with the
 * current strategy. If an inner node is selected, all children will be used.
 * 
 * @author felden@ira.uka.de
 * 
 */
@SuppressWarnings("serial")
public class AutoProofSubtreeAction extends BarAction implements
        PropertyChangeListener, InitialisingAction, Observer, Runnable {

    private static Icon goIcon = GUIUtil.makeIcon(AutoProofAction.class
            .getResource("img/cog_go.png"));
    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class
            .getResource("img/cog_stop.png"));

    private Job<Void> job = null;
    private boolean shouldStop;

    private ProofNode selectedProofNode;

    public AutoProofSubtreeAction() {
        super("Automatic Proof Subtree", goIcon);
        putValue(SHORT_DESCRIPTION, "Run automatic proving on the current node");
    }

    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF,
                this);
        getProofCenter().getProof().addObserver(this);

        getProofCenter().addPropertyChangeListener(
                ProofCenter.SELECTED_PROOFNODE, this);
        selectedProofNode = getProofCenter().getProof().getRoot();
    }

    public void actionPerformed(ActionEvent e) {

        Proof proof = getProofCenter().getProof();

        // if there are no open goals disable this action,
        // as the proof must have been closed
        if (!proof.hasOpenGoals()) {
            ExceptionDialog
                    .showExceptionDialog(getParentFrame(),
                            "Tried to proof an allready closed proof. This should not be allowed.");
            setEnabled(false);
            return;
        }

        if (job == null) {
            shouldStop = false;
            proof.getDaemon().addJob(this);
            getProofCenter()
                    .firePropertyChange(ProofCenter.ONGOING_PROOF, true);
        } else {
            shouldStop = true;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName()))
            selectedProofNode = (ProofNode) evt.getNewValue();
        else
            setIcon(((Boolean) evt.getNewValue()) ? stopIcon : goIcon);
    }

    @Override
    public void update(Observable o, Object arg) {
        Proof proof = (Proof) o;
        setEnabled(proof.hasOpenGoals());
    }

    public void run() {
        Proof proof = getProofCenter().getProof();
        ProofCenter pc = getProofCenter();
        Strategy strategy = pc.getStrategyManager().getSelectedStrategy();

        // if there are no open goals disable this action, as the
        // proof must have been closed
        if (!proof.hasOpenGoals()) {
            setEnabled(false);
            return;
        }

        List<ProofNode> todo = new LinkedList<ProofNode>();
        todo.add(selectedProofNode);

        try {
            strategy.init(proof, pc.getEnvironment(), pc.getStrategyManager());
            strategy.beginSearch();

            ProofNode current = null;

            while (!todo.isEmpty() && !shouldStop) {
                current = todo.remove(0);

                RuleApplication ra = strategy.findRuleApplication(current);

                if (ra != null) {
                    proof.apply(ra, pc.getEnvironment());
                    strategy.notifyRuleApplication(ra);

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
            getProofCenter().firePropertyChange(ProofCenter.ONGOING_PROOF,
                    false);
            job = null;
        }
    }
}