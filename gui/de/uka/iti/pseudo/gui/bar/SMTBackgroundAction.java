/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Icon;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.auto.Z3SMT;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.where.AskDecisionProcedure;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Pair;


@SuppressWarnings("serial") 
public class SMTBackgroundAction extends BarAction implements
        InitialisingAction, PropertyChangeListener, Observer, Runnable {

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
    private long timeout;
    
    /**
     * Cache to remember solvability of sequents.
     */
    private Map<Sequent, Boolean> sequentStatus =
        Collections.synchronizedMap(new HashMap<Sequent, Boolean>());
    
    /**
     * The synchronised blocking queue of proof nodes to be investigated.
     */
    private BlockingQueue<ProofNode> jobs = new LinkedBlockingQueue<ProofNode>();
    
    /**
     * The nodes which can be proven using Z3.
     */
    private Set<ProofNode> provableNodes = new HashSet<ProofNode>();
    
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

    /*
     * Instantiates a new SMT background action.
     * TODO tooltip
     */
    public SMTBackgroundAction() {
        Thread thread = new Thread(this, "SMT Background");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        
        // make images and set the non-flashing one
        noflashImg = BarManager.makeIcon(getClass().getResource("img/smt.gif"));
        flashImg = BarManager.makeIcon(getClass().getResource("img/smt_flash.gif"));
        setFlashing(false);
        
        // we will set us enabled after initialisation
        setEnabled(false);
    }

    /* 
     * retrieve the environment and read from it the necessary information, such as the
     * rule to apply, and the solver to use.
     */
    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
        proof = getProofCenter().getProof();
        proof.addObserver(this);
        env = getProofCenter().getEnvironment();
        
        closeRule = env.getRule(CLOSE_RULE_NAME);
        if(closeRule != null) {
            try {
                String className = closeRule.getProperty(AskDecisionProcedure.KEY_DECISION_PROCEDURE);
                solver = (DecisionProcedure) Class.forName(className).newInstance();
                timeout = Long.parseLong(closeRule.getProperty(AskDecisionProcedure.KEY_TIMEOUT));
            } catch(Exception ex) {
                System.err.println("Cannot start background decision procedure");
                ex.printStackTrace();
                closeRule = null;
            }
        }
        
        setEnabled(closeRule != null);
    }

    /* 
     * Try to prove all open goals.
     */
    public void actionPerformed(ActionEvent actionEvt) {

        // synchronise it with lock so that the thread does not tamper with provable nodes
        synchronized (proof) {
            List<ProofNode> openGoals = proof.getOpenGoals();
            for (int index = 0; index < openGoals.size(); index++) {
                MutableRuleApplication ra = new MutableRuleApplication();
                ra.setGoalNumber(index);
                ra.setRule(closeRule);
                try {
                    proof.apply(ra, env);
                } catch(ProofException ex) {
                  // this is ok - the goal may not be closeable.  
                } catch (Exception e) {
                    ExceptionDialog.showExceptionDialog(getParentFrame(), e);
                }
            }
        }
        
    }
    
    /* 
     * switch the button off when in proof elsewhere
     */
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled((Boolean) evt.getOldValue() && solver != null);
    }
    
    /* 
     * the proof object has changed. change our structures accordingly:
     * - remove from provable if no longer a goal
     * - set jobs to all newly open goals
     */
    public void update(Observable o, Object arg) {
        Iterator<ProofNode> it = provableNodes.iterator();
        while(it.hasNext()) {
            if(!proof.getOpenGoals().contains(it.next()))
                it.remove();
        }
        
        setFlashing(!provableNodes.isEmpty());
        
        jobs.clear();
        jobs.addAll(proof.getOpenGoals());
    }

    /*
     * flashing or non-flashing icon
     */
    private void setFlashing(boolean flashing) {
        setIcon(flashing ? flashImg : noflashImg);
    }

    /* 
     * perform a endless looping. Take one from the jobs and test for closability.
     * Add to provableNodes if so, cache the result.
     */
    public void run() {
        try {
            while(!Thread.interrupted()) {
                synchronized (lock) {
                    while(!backgroundActive && isEnabled()) {
                        lock.wait();
                    }
                }
                
                ProofNode pn = jobs.take();
                Sequent sequent = pn.getSequent();
                
                Boolean cached = sequentStatus.get(sequent);
                if(cached != null) {
                    if(cached) {
                        provableNodes.add(pn);
                        setFlashing(true);
                    }
                }
                
                try {
                    Pair<Result, String> result = solver.solve(sequent, env, timeout);
                    boolean proveable = result.fst() == Result.VALID;
                    sequentStatus.put(sequent, proveable);
                    if(proveable) {
                        provableNodes.add(pn);
                        setFlashing(true);
                    }
                } catch (Exception ex) {
                    ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
                    ex.printStackTrace();
                }
                
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the background thread active or not.
     * 
     * @param active
     *            the new background active
     */
    public void setBackgroundActive(boolean active) {
        synchronized (lock) {
            this.backgroundActive = active;
            if(active)
                lock.notify();
        }
            
    }
    
}
