/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
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

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

/**
 * This action tries to close a given list of open goals by searching for rule
 * applications with a thread pool. This will need strategies to be thread safe.
 *
 * In case "something is going on" (ONGOING_PROOF is set), firing the action
 * will raise a {@link ProofCenter#STOP_REQUEST} notification that listining
 * actions can react upon.
 *
 * @author felden@ira.uka.de
 */

public abstract class ParallelAutoProofAction extends BarAction implements PropertyChangeListener, InitialisingAction,
        NotificationListener {

    private static final long serialVersionUID = 7212654361200636678L;

    private static Icon stopIcon = GUIUtil.makeIcon(AutoProofAction.class.getResource("img/cog_stop.png"));
    private boolean ongoingProof = false;

    private ParallelAutoProofWorker job = null;

    public ParallelAutoProofAction(String name) {
        super(name);
        setIcon(getGoIcon());
    }

    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        getProofCenter().addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
    }

    /**
     * If the action is invoked, the reaction depends on whether there is an
     * ongoing proof. If so: bring up its modal dialog, otherwise create a new
     * job and run it and display the dialog.
     */
    @Override
    public void actionPerformed(ActionEvent event) {

        ProofCenter proofCenter = getProofCenter();
        Boolean ongoingProof = (Boolean) proofCenter.getProperty(ProofCenter.ONGOING_PROOF);

        // (be "null" safe here)
        if (ongoingProof == Boolean.TRUE) {
            // in case a proof is on its way notify the system with a stop request.
            proofCenter.fireNotification(ProofCenter.STOP_REQUEST);

        } else {
            // start auto proving
            job = new ParallelAutoProofWorker(proofCenter, getInitialList(), getParentFrame());

            proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, true);

            job.execute();
            // block ui with a modal dialog
            job.showDialog();
        }
    }

    @Override
    public void handleNotification(NotificationEvent evt) {
        if (!ongoingProof) {
            setEnabled(checkEnabled());
        }
    }

    /**
     * Check whether this action is to be enabled. This depends on the actual
     * implementation of this abstract class.
     *
     * @return <code>true</code> if this action should be enabled
     */
    protected abstract boolean checkEnabled();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Log.enter(evt);
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            ongoingProof = (Boolean) evt.getNewValue();
            setIcon(ongoingProof ? stopIcon : getGoIcon());
        } else if(ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName())) {
            if(!ongoingProof) {
                setEnabled(checkEnabled());
            }
        }
    }

    /**
     * Get the start list of proof nodes.
     *
     * @return A list of proof nodes; these will be recursively given to the
     *         current strategy and closed, if possible.
     */
    public abstract List<ProofNode> getInitialList();

    /**
     * retrieve the active icon for this action.
     */
    protected abstract Icon getGoIcon();

//    public void setJob(ParallelAutoProofWorker job) {
//        this.job = job;
//    }
//
//    public ParallelAutoProofWorker getJob() {
//        return job;
//    }
}
