/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.CompoundException;
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

    ParallelAutoProofWorker job = null;

    public ParallelAutoProofAction(String name) {
        super(name, goIcon);
    }

    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        getProofCenter().addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
    }

    /**
     * If the action is invoked, the reaction depends on whether there is an
     * ongoing proof. If so: bring up its modal dialog, otherwise create a new
     * job and run it and display the dialog.
     */
    public void actionPerformed(ActionEvent event) {
        
        if (job != null) {
            try {
                job.halt();
                setIcon(goIcon);
            } catch (CompoundException e) {
                Log.stacktrace(e);
                ExceptionDialog.showExceptionDialog(getParentFrame(), e);
                setIcon(goIcon);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // start auto proving
            final ProofCenter proofCenter = getProofCenter();

            job = new ParallelAutoProofWorker(proofCenter, this, getParentFrame());

            proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, true);
            
            job.execute();
            // block ui with a modal dialog
            job.showDialog();
        }
    }

    @Override
    public void handleNotification(NotificationEvent evt) {
        if (evt.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED) && !ongoingProof) {
            setEnabled(!getInitialList().isEmpty());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            ongoingProof = (Boolean) evt.getNewValue();
            setIcon(null != job ? stopIcon : goIcon);
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
