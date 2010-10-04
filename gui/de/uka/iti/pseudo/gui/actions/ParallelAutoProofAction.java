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

import javax.swing.Icon;
import javax.swing.SwingWorker;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.PooledAutoProver;

/**
 * This action tries to close a given list of open goals by searching for rule
 * applications with a thread pool. This will need strategies to be thread safe.
 * 
 * @author felden@ira.uka.de
 */
public abstract class ParallelAutoProofAction extends BarAction implements PropertyChangeListener, InitialisingAction,
        NotificationListener {

    private static final long serialVersionUID = 7212654361200636678L;

    private static Icon goIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_go.png"));
    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_stop.png"));
    private boolean ongoingProof = false;

    private PooledAutoProver pool = null;

    public ParallelAutoProofAction(String name) {
        super(name, goIcon);
    }

    public void initialised() {
        

        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        getProofCenter().addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
    }

    public void actionPerformed(ActionEvent e) {
        
        final ProofCenter proofCenter = getProofCenter();
        Strategy selectedStrategy = proofCenter.getStrategyManager().getSelectedStrategy();
        Environment environment = proofCenter.getEnvironment();

        if (null != pool) {
            
            try {
                pool.stopAutoProve(true);
            } catch (Exception ex) {
                Log.stacktrace(ex);
                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
            } finally {
                pool = null;
            }
            
        } else {
            // due to synchronisation, this must be invoked on the Dispatcher thread.
            pool = new PooledAutoProver(selectedStrategy, environment);

            SwingWorker<Void, Integer> swingWorker = new SwingWorker<Void, Integer>() {
                public Void doInBackground() {
//                    Proof proof = proofCenter.getProof();

//                    // if there are no open goals disable this action, as the
//                    // proof must have been closed
//                    // MU: happens automatically by ONGOING_PROOF ?!
//                    // MU: should be done on dispatch thread.
//                    if (!proof.hasOpenGoals()) {
//                        setEnabled(false);
//                        return null;
//                    }

                    for (ProofNode node : new LinkedList<ProofNode>(getInitialList())) {
                        pool.autoProve(node);
                    }

                    try {
                        pool.waitAutoProve();
                    } catch (Exception e) {
                        Log.stacktrace(e);
                        ExceptionDialog.showExceptionDialog(getParentFrame(), e);
                            e.printStackTrace();
                    }

                    return null;
                }

                public void done() {
                    proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
                    // some listeners have been switched off, they might
                    // want to update now.
                    proofCenter.fireProoftreeChangedNotification(true);
                    pool = null;
                }
            };

            proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, true);
            swingWorker.execute();
        }
    }

    @Override
    public void handleNotification(NotificationEvent evt) {
        // TODO ... is this what we want? Should depend on whether there are
        // open goals
        // under the currently selected node.
        if (evt.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED) && !ongoingProof) {
            Proof proof = getProofCenter().getProof();
            setEnabled(proof.hasOpenGoals());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            setIcon((ongoingProof = (Boolean) evt.getNewValue()) ? stopIcon : goIcon);
        }
    }

    /**
     * Get the start list of proof nodes.
     * 
     * @return A list of proof nodes; these will be recursively given to the
     *         current strategy and closed, if possible.
     */
    public abstract List<ProofNode> getInitialList();
}
