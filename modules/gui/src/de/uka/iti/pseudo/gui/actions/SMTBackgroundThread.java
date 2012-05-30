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

import java.awt.Frame;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;

public class SMTBackgroundThread extends Thread {

    private final SMTBackgroundAction action;

    /**
     * The lock used to synchronise the thread.
     */
    private final Object lock = new Object();

    /**
     * The synchronised blocking queue of proof nodes to be investigated.
     */
    private final BlockingQueue<ProofNode> jobs =
            new LinkedBlockingQueue<ProofNode>();

    private final ProofCenter proofCenter;

    /**
     * The property on ProofCenter that will be used to store the activation.
     */
    public static final String SMT_BACKGROUND_PROPERTY = "pseudo.smt.background";

    public SMTBackgroundThread(SMTBackgroundAction action, ProofCenter proofCenter) {
        super("SMT Background");
        this.action = action;
        this.proofCenter = proofCenter;
        setPriority(Thread.MIN_PRIORITY);
    }

    /*
     * perform a endless looping. Take one from the jobs and test for
     * closability. Add to provableNodes if so, cache the result.
     */
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (lock) {
                    while (!action.isBackgroundActive() || !action.isEnabled()) {
                        lock.wait();
                    }
                }

                ProofNode pn = jobs.take();

                try {
                    boolean provable = action.isProvable(pn);
                    if (provable) {
                        action.addProvableNode(pn);
                    }
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            proofCenter.firePropertyChange(SMT_BACKGROUND_PROPERTY, false);
                            Frame parentFrame = action.getParentFrame();
                            ExceptionDialog.showExceptionDialog(parentFrame, ex);
                            JOptionPane.showMessageDialog(parentFrame,
                                    "'Background SMT' will be switched off to stop repeating "
                                    + "failures. You can reenable it in the settings menu");
                        }
                    });
                }
            }
        } catch (InterruptedException e) {
            Log.stacktrace(Log.WARNING, e);
        }
        Log.log(Log.WARNING, "SMT Background thread has terminated - this should not happen");
    }

    public void notifyContinue(List<ProofNode> openGoals) {
        synchronized (lock) {
            jobs.clear();
            Log.log(Log.VERBOSE, "New jobs queue: " + openGoals);
            jobs.addAll(openGoals);
            lock.notify();
        }
    }

}
