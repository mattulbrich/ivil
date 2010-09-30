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
import java.util.concurrent.Callable;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.PooledAutoProofer;

/**
 * This action tries to close a given list of open goals by searching for rule
 * applications with a thread pool. This will need strategys to be threadsafe.
 * 
 * @author felden@ira.uka.de
 */
public abstract class ParallelAutoProofAction extends BarAction implements PropertyChangeListener, InitialisingAction,
        Runnable, NotificationListener {

    private static final long serialVersionUID = 7212654361200636678L;

    class RuleApplicationFinder implements Callable<RuleApplication> {
        final private ProofNode target;
        final private Strategy strategy;

        public RuleApplicationFinder(final ProofNode target, final Strategy strategy) {
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

    private static Icon goIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_go.png"));
    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_stop.png"));

    private boolean hasJob = false;
    private PooledAutoProofer pool;

    public ParallelAutoProofAction(String name) {
        super(name, goIcon);
    }

    public void initialised() {
        pool = new PooledAutoProofer(getProofCenter().getStrategyManager().getSelectedStrategy(), getProofCenter()
                .getEnvironment());

        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        getProofCenter().addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
    }

    public void actionPerformed(ActionEvent e) {
        final Proof proof = getProofCenter().getProof();

        // if there are no open goals disable this action,
        // as the proof must have been closed
        if (!proof.hasOpenGoals()) {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Tried to proof an allready closed proof. This should not be allowed.");
            setEnabled(false);
            return;
        }

        if (hasJob) {
            pool.stopAutoProof(true);
            hasJob = false;
        } else {
            hasJob = true;

            getProofCenter().firePropertyChange(ProofCenter.ONGOING_PROOF, true);

            // FIXME CREATE WORKER
            SwingUtilities.invokeLater(this);
        }
    }

    @Override
    public void handleNotification(NotificationEvent evt) {
        // TODO ... is this what we want? Should depend on whether there are
        // open goals
        // under the currently selected node.
        if (evt.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED)) {
            Proof proof = getProofCenter().getProof();
            setEnabled(proof.hasOpenGoals());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // FIXME?! Really? For the embedded action you want to change the icon?
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            setIcon(((Boolean) evt.getNewValue()) ? stopIcon : goIcon);
        }
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
        final ProofCenter pc = getProofCenter();
        final Proof proof = pc.getProof();
        final Strategy strategy = pc.getStrategyManager().getSelectedStrategy();
        final Environment env = pc.getEnvironment();

        // if there are no open goals disable this action, as the
        // proof must have been closed
        if (!proof.hasOpenGoals()) {
            setEnabled(false);
            return;
        }

        for (ProofNode node : new LinkedList<ProofNode>(getInitialList())) {
            pool.autoProof(node, strategy, env);
        }

        // FIXME put this in the after-work part of a SwingWorker
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                pc.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
                // some listeners have been switched off, they might want to
                // update now.
                pc.fireNotification(ProofCenter.PROOFTREE_HAS_CHANGED);
                hasJob = false;
            }
        });
    }
}
