/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

/**
 * This is the action which is on the SMT button.
 *
 * Pressing it while flashing will close all nodes, that are known to be
 * closable. Pressing it while not flashing will cause it to feed all open goals
 * to the SMT solver.
 */
@SuppressWarnings("serial")
public final class SMTBackgroundAction extends SMTAction
    implements NotificationListener {

    /**
     * image resources.
     */
    private final Icon noflashImg;
    private final Icon flashImg;

    /**
     * This action can be made inactive
     */
    private boolean backgroundActive;

    /**
     * The thread on which the background checking runs
     */
    private SMTBackgroundThread thread;

    /**
     * The nodes for which we that they can be proven using Z3.
     */
    private final List<ProofNode> provableNodes =
            Collections.synchronizedList(new LinkedList<ProofNode>());

    /**
     * Tooltip iff flashing
     */
    private static final String TOOLTIP_FLASHING =
            "Some goals can be closed by the SMT solver. Close them!";

    /**
     * Tooltip iff not flashing
     */
    private static final String TOOLTIP_NOT_FLASHING =
            "Run the STM solver on all open goals.";

    /*
     * Instantiates a new SMT background action.
     */
    public SMTBackgroundAction() {
        super("auto_smt_close");

        // make images and set the non-flashing one
        noflashImg = GUIUtil.makeIcon(getClass().getResource("img/smt.gif"));
        flashImg = GUIUtil.makeIcon(getClass().getResource("img/smt_flash.gif"));
        setFlashing(false);
    }

    /*
     * retrieve the environment and read from it the necessary information, such
     * as the rule to apply, and the solver to use.
     */
    @Override
    public void initialised() {
        super.initialised();
        ProofCenter proofCenter = getProofCenter();

        proofCenter.addPropertyChangeListener(SMTBackgroundThread.SMT_BACKGROUND_PROPERTY, this);
        proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
        proofCenter.addNotificationListener(ProofCenter.TERMINATION, this);

        // Start bg process
        this.thread = new SMTBackgroundThread(this, proofCenter);
        thread.start();

        setBackgroundActive((Boolean) proofCenter.getProperty(SMTBackgroundThread.SMT_BACKGROUND_PROPERTY));
    }

    /*
     * switch the button off when in proof elsewhere according to the settings,
     * activate or deactivate the background thread
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);

        if (SMTBackgroundThread.SMT_BACKGROUND_PROPERTY.equals(evt.getPropertyName())) {
            setBackgroundActive((Boolean) evt.getNewValue());
        }
    }

    /*
     * Sets the background thread active or not.
     */
    private void setBackgroundActive(boolean act) {
        this.backgroundActive = act;
        if (backgroundActive) {
            thread.notifyContinue(proof.getOpenGoals());
        }
    }

    /*
     * flashing or non-flashing icon and change tooltip
     */
    private void setFlashing(boolean flashing) {
        setIcon(flashing ? flashImg : noflashImg);
        putValue(SHORT_DESCRIPTION, flashing ? TOOLTIP_FLASHING : TOOLTIP_NOT_FLASHING);
    }

    /*
     * Delegate the actual proving to a SwingWorker.
     * Set into ongoing proof mode beforehand.
     */
    @Override
    public void actionPerformed(ActionEvent actionEvt) {
        getProofCenter().firePropertyChange(ProofCenter.ONGOING_PROOF, true);
        SMTBackgroundWorker worker = new SMTBackgroundWorker(this, getProofCenter());
        worker.execute();
    }

    public boolean isBackgroundActive() {
        return backgroundActive;
    }

    public void addProvableNode(ProofNode pn) {
        provableNodes.add(pn);
        // TODO should happen on EventQueue
        setFlashing(true);
    }

    /**
     * the proof object has changed. change our structures accordingly:
     * <ul>
     * <li>remove nodes from provable if no longer a goal
     * <li>set jobs to all newly open goals
     * </ul>
     */
    @Override
    public void handleNotification(NotificationEvent event) {
        assert SwingUtilities.isEventDispatchThread();

        if (event.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED)) {
            // no update while in automatic proof
            if ((Boolean) getProofCenter().getProperty(ProofCenter.ONGOING_PROOF)) {
                return;
            }

            List<ProofNode> openGoals = getProofCenter().getProof().getOpenGoals();
            provableNodes.retainAll(openGoals);

            setFlashing(!provableNodes.isEmpty());

            thread.notifyContinue(openGoals);
        } else

        if (event.isSignal(ProofCenter.TERMINATION)) {
            if(thread != null) {
                this.thread.interrupt();
            }
        }
    }

    @Override
    public List<ProofNode> getProvableNodes() {
        return provableNodes;
    }

    @Override
    public String getWindowTitle() {
        return "Applying the SMT solver";
    }


}


