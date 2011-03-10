/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.util.SwingWorker2;
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
     * The property on ProofCenter that will be used to decide whether or not to
     * close the window after completion.
     */
    public static final String SMT_KEEPWINDOWOPEN_PROPERTY = "pseudo.smt.keepwindowopen";
    
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
    private static final String TOOLTIP_FLASHING = "Some goals can be closed by the SMT solver. Close them!";
    /**
     * Tooltip iff not flashing
     */
    private static final String TOOLTIP_NOT_FLASHING = "Run the STM solver on all open goals.";

    /*
     * Instantiates a new SMT background action.
     */
    public SMTBackgroundAction() {
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
        
        // Start bg process
        Thread thread = new Thread(this, "SMT Background");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

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
            
            List<ProofNode> openGoals = proof.getOpenGoals();
            provableNodes.retainAll(openGoals);

            setFlashing(!provableNodes.isEmpty());

            jobs.clear();
            Log.log(Log.VERBOSE, "New jobs queue: " + openGoals);
            jobs.addAll(openGoals);
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
                
                try {
                    boolean provable = isProvable(pn);
                    if (provable) {
                        provableNodes.add(pn);
                        setFlashing(true);
                    }       
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getProofCenter().firePropertyChange(SMT_BACKGROUND_PROPERTY, false);
                            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
                            JOptionPane.showMessageDialog(getParentFrame(),
                                    "'Background SMT' will be switched off to stop repeating "
                                    + "failures. You can reenable it in the settings menu");
                        }
                    });
                }
            }
        } catch (InterruptedException e) {
            Log.stacktrace(e);
            e.printStackTrace();
        }
    }

    private boolean isProvable(ProofNode pn) throws ProofException, IOException {
        
        Sequent sequent = pn.getSequent();
        Boolean cached = sequentStatus.get(sequent);
        if (cached != null) {
            Log.log(Log.VERBOSE, "Provability cache hit for " + pn + ": " + cached);
            return cached.booleanValue();
        } else {
            Pair<Result, String> result = solver.solve(sequent, env, timeout);
            boolean proveable = result.fst() == Result.VALID;
            sequentStatus.put(sequent, proveable);
            Log.log(Log.VERBOSE, "Provability result for " + pn + ": " + result);
            return proveable;
        }                
    }
    
    /*
     * Delegate the actual proving to a SwingWorker.
     * Set into ongoing proof mode beforehand.
     */
    public void actionPerformed(ActionEvent actionEvt) {
        
        getProofCenter().firePropertyChange(ProofCenter.ONGOING_PROOF, true);
        new Worker().execute();
        
    }

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
    private class Worker extends SwingWorker2<Void, String> implements NotificationListener {

        /**
         * The labels into which the results are to be reported.
         */
        private List<JLabel> resultLabels = new LinkedList<JLabel>();
        
        /**
         * The dialog which reports about the progress.
         */
        private JDialog dialog;
        
        /**
         * This worker should listen to stop requests (if run in background)
         */
        public Worker() {
            getProofCenter().addNotificationListener(ProofCenter.STOP_REQUEST, this);
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            
            ProofCenter proofCenter = getProofCenter();
                if (!provableNodes.isEmpty()) {

                    // automatic rules: do not bother with window & rules, just do it.
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
                    
                    // TODO is this thread safe with the producer?!?!
                    provableNodes.clear();

                } else {
                    
                    // bugfix: clone the list first
                    final List<ProofNode> openGoals =
                        new ArrayList<ProofNode>(proof.getOpenGoals());
                    
                    // trigger the progress window
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            makeProgressWindow(openGoals);
                        }
                    });
                    
                    for (ProofNode proofNode : openGoals) {
                        
                        // check for cache hit
                        boolean proveable = isProvable(proofNode);
                        
                        if(proveable) {
                            MutableRuleApplication ra = new MutableRuleApplication();
                            ra.setProofNode(proofNode);
                            ra.setRule(closeRule);
                           
                            try {
                                proofCenter.apply(ra);
                                publish("CLOSED");
                            } catch (ProofException e) {
                                publish("exception");
                                throw e;
                            }
                        } else {
                            publish("open");
                        }
                        
                    }
                }
            
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
                ExceptionDialog.showExceptionDialog(getParentFrame(), innerException);
            }
            
            if(dialog != null && 
                    getProofCenter().getProperty(SMT_KEEPWINDOWOPEN_PROPERTY) != Boolean.TRUE) {
                dialog.dispose();
            }
            
            ProofCenter proofCenter = getProofCenter();
            proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
            proofCenter.fireProoftreeChangedNotification(true);
            proofCenter.removeNotificationListener(ProofCenter.STOP_REQUEST, this);
        }
        
        /**
         * provide feedback on closed goals. {@link #doInBackground()} reports
         * boolean values: True if a goal is closable, false if not. 
         */
        @Override
        protected void process(List<String> chunks) {
            
            for (String result : chunks) {
                JLabel label = resultLabels.remove(0);
                label.setText(label.getText() + " " + result);
            }
        }
        

        protected void makeProgressWindow(List<ProofNode> openGoals) {
            dialog = new JDialog(getParentFrame(), "Applying the SMT solver", true);
            
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            JPanel panel = new JPanel();
            {
                panel.setLayout(new GridBagLayout());
                dialog.getContentPane().add(panel);
                panel.setBorder(BorderFactory.createTitledBorder("Open goals"));
            }
            
            int count = openGoals.size();
            for (int i = 0; i < count; i++) {
                ProofNode goal = openGoals.get(i);
                JLabel label = new JLabel("Node " + goal.getNumber() + ":");
                label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                resultLabels.add(label);
                panel.add(label,
                        new GridBagConstraints(0, i, 1, 1, 0, 1,
                                GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
                                        2, 2, 2, 2), 0, 0));
            }
            
            {
                JCheckBox keepOpen = new JCheckBox("Keep open after completion");
                keepOpen.setSelected(getProofCenter().getProperty(SMT_KEEPWINDOWOPEN_PROPERTY) == Boolean.TRUE);
                keepOpen.addItemListener(new ItemListener() {
                    @Override public void itemStateChanged(ItemEvent e) {
                        Log.enter(e);
                        boolean selectionState = e.getStateChange() == ItemEvent.SELECTED;
                        getProofCenter().firePropertyChange(SMT_KEEPWINDOWOPEN_PROPERTY, selectionState);
                    }
                });
                panel.add(keepOpen, new GridBagConstraints(0, count, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
                                10, 2, 2, 2), 0, 0));
            }
            
            JPanel buttons = new JPanel();
            panel.add(buttons, new GridBagConstraints(0, count+1, 1, 1, 0, 0,
                    GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
                            5, 2, 2, 2), 0, 0));
            {
                JButton stop = new JButton("Stop/Close");
                stop.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(isDone()) {
                            dialog.dispose();
                        } else {
                            cancel(true);
                        }
                    }
                });
                buttons.add(stop);
            }
            {
                // TODO Have a skip button to jump to next goal.
                JButton skip = new JButton("Skip");
                buttons.add(skip);
                skip.setEnabled(false);
            }
            {
                JButton bg = new JButton("Background");
                bg.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.setVisible(false);
                    }
                });
                buttons.add(bg);
            }
            
            dialog.pack();
            dialog.setResizable(false);
            dialog.setLocationRelativeTo(getParentFrame());
            dialog.setVisible(true);
        }

        @Override
        public void handleNotification(NotificationEvent event) {
            Log.enter(event);
            cancel(true);            
        }

    }

}


