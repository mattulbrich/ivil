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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.rule.where.AskDecisionProcedure;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.Pair;

// Class is final because thread is started in constructor which is evil 
// for subclassing.
/**
 * This is the action which is on the SMT button.
 * 
 * Pressing it while flashing will close all nodes, that are known to be
 * closable. Pressing it while not flashing will cause it to feed all open goals
 * to the SMT solver.
 */
@SuppressWarnings("serial")
public final class SMTBackgroundAction extends BarAction implements InitialisingAction, PropertyChangeListener,
        Runnable, NotificationListener {

    /**
     * The property on ProofCenter that will be used to store the activation.
     */
    public static final String SMT_BACKGROUND_PROPERTY = "pseudo.smt.background";

    /**
     * The rule to be called to close goals with
     */
    private static final String CLOSE_RULE_NAME = "auto_smt_close";

    /**
     * The solver used to determine the status. Retrieved from the rule named
     * {@value #CLOSE_RULE_NAME} using the key
     * {@link AskDecisionProcedure#KEY_DECISION_PROCEDURE}
     */
    private DecisionProcedure solver;

    /**
     * The timeout to be used by the solver. Retrieved from the rule named
     * {@value #CLOSE_RULE_NAME} using the key
     * {@link AskDecisionProcedure#KEY_TIMEOUT}
     */
    private int timeout;

    /**
     * Cache to remember solvability of sequents.
     * 
     * We use a weak hash map to allow freeing if space is needed.
     */
    private Map<Sequent, Boolean> sequentStatus = Collections.synchronizedMap(new WeakHashMap<Sequent, Boolean>());

    /**
     * The synchronised blocking queue of proof nodes to be investigated.
     */
    private BlockingQueue<ProofNode> jobs = new LinkedBlockingQueue<ProofNode>();

    /**
     * The nodes which can be proven using Z3.
     */
    private List<ProofNode> provableNodes = Collections.synchronizedList(new LinkedList<ProofNode>());

    /**
     * The lock used to synchronise the thread.
     */
    private Object lock = new Object();

    /**
     * The proof element we are working on.
     */
    private Proof proof;

    /**
     * The environment is needed to provide the rules.
     */
    private Environment env;

    /**
     * image resources.
     */
    private Icon noflashImg;
    private Icon flashImg;

    /**
     * This action can be made inactive
     */
    private boolean backgroundActive;

    /**
     * The rule to close by Z3.
     */
    private Rule closeRule;

    /**
     * Tooltip iff flashing
     */
    private static final String TOOLTIP_FLASHING = "Closes all nodes, that are known to be closable.";
    /**
     * Tooltip iff not flashing
     */
    private static final String TOOLTIP_NOT_FLASHING = "Tries to close all open goals using the smt solver.";

    /*
     * Instantiates a new SMT background action.
     */
    public SMTBackgroundAction() {
        Thread thread = new Thread(this, "SMT Background");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        // make images and set the non-flashing one
        noflashImg = GUIUtil.makeIcon(getClass().getResource("img/smt.gif"));
        flashImg = GUIUtil.makeIcon(getClass().getResource("img/smt_flash.gif"));
        setFlashing(false);

        // we will set us enabled after initialisation
        setEnabled(false);
    }

    /*
     * retrieve the environment and read from it the necessary information, such
     * as the rule to apply, and the solver to use.
     */
    public void initialised() {
        ProofCenter proofCenter = getProofCenter();

        proof = proofCenter.getProof();
        proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
        env = proofCenter.getEnvironment();

        proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);

        proofCenter.addPropertyChangeListener(SMT_BACKGROUND_PROPERTY, this);
        setBackgroundActive((Boolean) proofCenter.getProperty(SMT_BACKGROUND_PROPERTY));

        closeRule = env.getRule(CLOSE_RULE_NAME);
        if (closeRule != null) {
            try {
                String className = closeRule.getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
                solver = (DecisionProcedure) Class.forName(className).newInstance();
                timeout = Integer.parseInt(closeRule.getProperty(RuleTagConstants.KEY_TIMEOUT));
            } catch (Exception ex) {
                Log.log(Log.WARNING, "Cannot instantiate background decision procedure");
                ex.printStackTrace();
                closeRule = null;
            }
        }

        setEnabled(closeRule != null);

    }

    /*
     * If goals are provable, close them and don't start proving all goals if
     * background SMT is active. Else try to prove all open goals.
     */
    public void actionPerformed(ActionEvent actionEvt) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProofCenter proofCenter = getProofCenter();

                try {
                    if (backgroundActive && !provableNodes.isEmpty()) {

                        for (ProofNode node : provableNodes) {
                            // the node might have been closed already somehow,
                            // as this is a multi threaded environment
                            if (node.isClosed())
                                continue;

                            MutableRuleApplication ra = new MutableRuleApplication();
                            ra.setProofNode(node);
                            ra.setRule(closeRule);
                            try {
                                proofCenter.apply(ra);

                            } catch (Exception e) {
                                ExceptionDialog.showExceptionDialog(getParentFrame(), e);
                            }
                        }

                    } else {
                        List<ProofNode> openGoals = proof.getOpenGoals();
                        int countGoals = openGoals.size();
                        // bugfix: do it backward, otherwise solving goal 0 and
                        // incrementing would not do the second original goal
                        // which
                        // has become the new 0
                        for (int index = countGoals - 1; index >= 0; index--) {
                            MutableRuleApplication ra = new MutableRuleApplication();
                            ra.setProofNode(openGoals.get(index));
                            ra.setRule(closeRule);
                            try {
                                proofCenter.apply(ra);

                            } catch (ProofException ex) {
                                Log.stacktrace(Log.VERBOSE, ex);
                                // this is ok - the goal may be not closable.
                            } catch (Exception e) {
                                ExceptionDialog.showExceptionDialog(getParentFrame(), e);
                            }
                        }
                    }
                } finally {
                    proofCenter.fireProoftreeChangedNotification(true);
                }
            }
        });
    }

    /*
     * switch the button off when in proof elsewhere according to the settings,
     * activate or deactivate the background thread
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            setEnabled(!(Boolean) evt.getNewValue() && solver != null);
        } else

        if (SMT_BACKGROUND_PROPERTY.equals(evt.getPropertyName())) {
            setBackgroundActive((Boolean) evt.getNewValue());
        } else

        {
            assert false : "Case distinction failed";
        }
    }

    /*
     * Sets the background thread active or not.
     */
    private void setBackgroundActive(boolean act) {
        synchronized (lock) {
            this.backgroundActive = act;
            if (backgroundActive) {
                jobs.clear();
                jobs.addAll(proof.getOpenGoals());
                lock.notify();
            }
        }
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

            Iterator<ProofNode> it = provableNodes.iterator();
            while (it.hasNext()) {
                if (!proof.getOpenGoals().contains(it.next()))
                    it.remove();
            }

            setFlashing(!provableNodes.isEmpty());

            jobs.clear();
            jobs.addAll(proof.getOpenGoals());
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
     * perform a endless looping. Take one from the jobs and test for
     * closability. Add to provableNodes if so, cache the result.
     */
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (lock) {
                    while (!backgroundActive || !isEnabled()) {
                        lock.wait();
                    }
                }

                ProofNode pn = jobs.take();
                Sequent sequent = pn.getSequent();

                Boolean cached = sequentStatus.get(sequent);
                if (cached != null) {
                    if (cached) {
                        provableNodes.add(pn);
                        setFlashing(true);
                    }
                } else {
                    try {
                        Pair<Result, String> result = solver.solve(sequent, env, timeout);
                        boolean proveable = result.fst() == Result.VALID;
                        sequentStatus.put(sequent, proveable);
                        if (proveable) {
                            provableNodes.add(pn);
                            setFlashing(true);
                        }
                    } catch (final Exception ex) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
                                JOptionPane.showMessageDialog(getParentFrame(),
                                        "'Background SMT' will be switched off to stop repeating "
                                                + "failures. You can reenable it in the settings menu");
                                getProofCenter().firePropertyChange(SMT_BACKGROUND_PROPERTY, false);
                            }
                        });
                    }
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
