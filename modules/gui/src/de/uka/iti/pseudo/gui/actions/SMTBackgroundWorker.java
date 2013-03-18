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

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.util.SwingWorker2;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.Pair;

/**
 * A two-thread worker which acts when the action is performed.
 *
 * It has two modes:
 * <ol>
 * <li>Flashing mode: Whenever there are entries in {@link #provableNodes},
 * only apply the according rules. This is not very time consuming since the
 * SMT solver needs not run a second time.
 * <li>Non-flashing mode: If there are no known closable goals, apply the
 * SMT to all available goals with a longer timeout. Open a window which
 * reports on the progress.
 * </ol>
 *
 */
 class SMTBackgroundWorker extends SwingWorker2<Void, Pair<Integer, String>>
        implements NotificationListener {

    /**
     * The dialog which reports about the progress.
     */
    private SMTProgressDialog dialog;

    private final SMTAction action;

    private final ProofCenter proofCenter;

    private ArrayList<ProofNode> openGoals;

    /**
     * This worker should listen to stop requests (if run in background)
     */
    public SMTBackgroundWorker(SMTAction smtAction, ProofCenter proofCenter) {
        this.action = smtAction;
        this.proofCenter = proofCenter;
        proofCenter.addNotificationListener(ProofCenter.STOP_REQUEST, this);
        proofCenter.addNotificationListener(ProofCenter.TERMINATION, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground() throws Exception {
        Log.enter();
        assert (Boolean)proofCenter.getProperty(ProofCenter.ONGOING_PROOF);

        List<ProofNode> provableNodes =
                new ArrayList<ProofNode>(action.getProvableNodes());

        if (!provableNodes.isEmpty()) {

            // automatic rules: do not bother with window & rules, just do it.
            for (ProofNode node : provableNodes) {
                // the node might have been worked on already somehow,
                // as this is a multi-threaded environment
                if (node.getChildren() != null) {
                    continue;
                }

                MutableRuleApplication ra = new MutableRuleApplication();
                ra.setProofNode(node);
                ra.setRule(action.getCloseRule());
                try {
                    proofCenter.apply(ra);
                } catch (Exception e) {
                    ExceptionDialog.showExceptionDialog(action.getParentFrame(), e);
                }
            }

            // In done() we call this which settles it:
            // proofCenter.fireProoftreeChangedNotification(false);

        } else {

            // bugfix: clone the list first
            openGoals = new ArrayList<ProofNode>(proofCenter.getProof().getOpenGoals());

            // trigger the window;
            for (ProofNode proofNode : openGoals) {
                Boolean status = action.getStatus(proofNode.getSequent());
                if(status != null) {
                    String text = status ? "(cached VALID)" : "(cached open)";
                    publish(Pair.<Integer, String>make(proofNode.getNumber(), text));
                }
            }

            for (ProofNode proofNode : openGoals) {
                int number = proofNode.getNumber();
                if(isCancelled()) {
                    Log.log(Log.VERBOSE, "SMT background worker interrupted");
                    return null;
                }

                publish(Pair.<Integer, String>make(number, "--- checking ---"));
                // check for cache hit or solve it
                boolean proveable = action.isProvable(proofNode);

                if(proveable) {
                    MutableRuleApplication ra = new MutableRuleApplication();
                    ra.setProofNode(proofNode);
                    ra.setRule(action.getCloseRule());

                    try {
                        proofCenter.apply(ra);
                        publish(Pair.<Integer, String>make(number, "CLOSED"));
                    } catch (ProofException e) {
                        publish(Pair.<Integer, String>make(number, "exception"));
                        throw e;
                    }
                } else {
                    publish(Pair.<Integer, String>make(number, "open"));
                }
            }
        }

        Log.leave();
        return null;
    }

    /**
     * in the end: indicate end of ongoing proof, fire tree change and
     * unregister as notification listener.
     */
    @Override
    protected void done() {

        Exception innerException = getException();
        if(innerException != null) {
            ExceptionDialog.showExceptionDialog(action.getParentFrame(), innerException);
        }

        if(dialog != null) {
            dialog.finished();
            if(proofCenter.getProperty(SMTProgressDialog.SMT_KEEPWINDOWOPEN_PROPERTY) != Boolean.TRUE) {
                dialog.dispose();
            }
        }

        proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
        proofCenter.fireProoftreeChangedNotification(true);
        proofCenter.removeNotificationListener(ProofCenter.STOP_REQUEST, this);
    }

    /**
     * provide feedback on closed goals. {@link #doInBackground()} reports
     * boolean values: True if a goal is closable, false if not.
     */
    @Override
    protected void process(List<Pair<Integer, String>> chunks) {
        try {
            if(dialog == null) {
                showProgressWindow();
            }

            dialog.addResults(chunks);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void showProgressWindow() {
        dialog = new SMTProgressDialog(action.getParentFrame(),
                action.getWindowTitle(), action.getProofCenter());
        dialog.showDialog();
    }

    // act on STOP_REQUEST
    @Override
    public void handleNotification(NotificationEvent event) {
        Log.enter(event);
        cancel(true);
    }

}
