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
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.Icon;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofDaemon.Job;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * This action tries to close a given list of open goals by searching for rule
 * applications with a thread pool. This will need strategys to be threadsafe.
 * 
 * @author felden@ira.uka.de
 */
@SuppressWarnings("serial")
public abstract class ParallelAutoProofAction extends BarAction implements
        PropertyChangeListener, InitialisingAction, Observer, Runnable {

    class RuleApplicationFinder implements Callable<RuleApplication> {
        final private ProofNode target;
        final private Strategy strategy;

        public RuleApplicationFinder(final ProofNode target,
                final Strategy strategy) {
            this.target = target;
            this.strategy = strategy;
        }

        public RuleApplication call() {
            try {
                return strategy.findRuleApplication(target);
            } catch (StrategyException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static Icon goIcon = GUIUtil.makeIcon(AutoProofAction.class
            .getResource("img/cog_go.png"));
    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class
            .getResource("img/cog_stop.png"));

    private Job<Void> job = null;
    private boolean shouldStop = false;
    private ExecutorService pool = Executors.newFixedThreadPool(16);

    public ParallelAutoProofAction(String name) {
        super(name, goIcon);
    }

    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF,
                this);
        getProofCenter().getProof().addObserver(this);
    }

    public void actionPerformed(ActionEvent e) {
        final Proof proof = getProofCenter().getProof();

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
            getProofCenter()
                    .firePropertyChange(ProofCenter.ONGOING_PROOF, true);
            proof.getDaemon().addJob(this);
        } else {
            shouldStop = true;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setIcon(((Boolean) evt.getNewValue()) ? stopIcon : goIcon);
    }

    @Override
    public void update(Observable o, Object arg) {
        Proof proof = (Proof) o;
        setEnabled(proof.hasOpenGoals());
    }

    /**
     * Get the start list of proof nodes.
     * 
     * @return A list of proof nodes; these will be recursively given to the
     *         current strategy and closed, if possible.
     */
    public abstract List<ProofNode> getInitialList();

    @Override
    public void run() {
        final Proof proof = getProofCenter().getProof();
        final ProofCenter pc = getProofCenter();
        final Strategy strategy = pc.getStrategyManager().getSelectedStrategy();

        // if there are no open goals disable this action, as the
        // proof must have been closed
        if (!proof.hasOpenGoals()) {
            setEnabled(false);
            return;
        }

        List<ProofNode> todo = new LinkedList<ProofNode>(getInitialList());
        // Queue<Future<RuleApplication>> applications = new
        // LinkedList<Future<RuleApplication>>();
        ProofNode current;
        RuleApplication ra;

        try {
            strategy.init(proof, pc.getEnvironment(), pc.getStrategyManager());
            strategy.beginSearch();

            while (!todo.isEmpty() && !shouldStop) {
                current = todo.remove(0);
                ra = strategy.findRuleApplication(current);

                if (ra != null) {
                    proof.apply(ra, pc.getEnvironment());
                    strategy.notifyRuleApplication(ra);

                    for (ProofNode node : current.getChildren())
                        todo.add(node);
                }
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
