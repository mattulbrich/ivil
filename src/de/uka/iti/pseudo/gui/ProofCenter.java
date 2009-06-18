/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.RulePriorityComparator;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Sequent;

/**
 * The Class ProofCenter is the center point of one proof and its visualiation
 * across several components.
 * 
 * It keeps references to UI the main window as top level gui element.
 * 
 * It allows access to the proof object of the currently displayed proof as well
 * as to the used environment. Proof steps can be performed by calling
 * {@link #apply(RuleApplication)}.
 * 
 * An instance serves also as node selection event manager. when a proof node is
 * selected in a component (goal list, proof tree, ...), a component can call
 * {@link #fireSelectedProofNode(ProofNode)} and all registered
 * {@link ProofNodeSelectionListener} are informed of the selection.
 * 
 * It acts also as {@link TermSelectionListener} which reacts on term selection
 * (right click) on a term in the sequent view, see
 * {@link #termSelected(Sequent, TermSelector)}.
 * 
 */
public class ProofCenter implements TermSelectionListener {
    
    /**
     * Constants used as tags in property clauses in rule definitions
     */
    private static final String AUTOONLY_TAG = "autoonly";

    /**
     * The main window.
     */
    private MainWindow mainWindow;
    
    /**
     * The used environment.
     */
    private Environment env;
    
    /**
     * The proof showed in the main window. 
     */
    private Proof proof;
    
    /**
     * All rules of the environment, sorted for interaction.
     * (at the moment not sorted at all)
     */
    private List<Rule> rulesSortedForInteraction;
    
    /**
     * the list of registered {@link ProofNodeSelectionListener}.
     */
    private List<ProofNodeSelectionListener> listeners = new LinkedList<ProofNodeSelectionListener>();
    
    /**
     * for synchronisation: Is this center currently firing a message? 
     * If so, do not start another firing.
     */
    private boolean isFiring = false;
    
    /**
     * The currently selected proof node.
     */
    private ProofNode currentProofNode;
    
    /**
     * Instantiates a new proof center.
     * 
     * @param proof
     *            the proof to use
     * @param env
     *            the environment
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred. This happens if
     *             the {@link BarManager} cannot find resources
     */
    public ProofCenter(@NonNull Proof proof, @NonNull Environment env)  throws IOException {
        this.proof = proof;
        this.env = env;
        mainWindow = new MainWindow(this, env.getResourceName());
        mainWindow.makeGUI();
        fireSelectedProofNode(proof.getRoot());
        
        prepareRuleLists();
        
        mainWindow.firePropertyChange(MainWindow.INITIALISED, true);
    }

    /*
     * Prepare rule lists.
     * 
     * interactive rules are sorted by priority.
     */
    private void prepareRuleLists() {
        rulesSortedForInteraction = env.getAllRules();
        
        Iterator<Rule> it = rulesSortedForInteraction.iterator();
        while (it.hasNext()) {
            Rule rule = it.next();
            if(rule.getProperty(AUTOONLY_TAG) != null)
                it.remove();
        }
        Collections.sort(rulesSortedForInteraction, new RulePriorityComparator());
        
        // other rule lists: simplifications with priority codes.
    }

    /**
     * Gets the environment.
     * 
     * @return the environment
     */
    public @NonNull Environment getEnvironment() {
        return env;
    }

    /**
     * Gets the proof.
     * 
     * @return the proof
     */
    public @NonNull Proof getProof() {
        return proof;
    }

    /**
     * Gets the main window.
     * 
     * @return the main window
     */
    public @NonNull MainWindow getMainWindow() {
        return mainWindow;
    }
    
    /**
     * Registers a proof node selection listener.
     * 
     * @param l the listener
     */
    public void addProofNodeSelectionListener(ProofNodeSelectionListener l) {
        listeners.add(l);
    }
    
    /**
     * Unregisters a proof node selection listener.
     * 
     * @param l the listener
     */
    public void removeProofNodeSelectionListener(ProofNodeSelectionListener l) {
        listeners.remove(l);
    }
    
    /**
     * Indicate that a proof node has been selected.
     * 
     * All registered proof node selection listeners are informed of this
     * selection. The notification is ensured to be run on the swing event queue
     * thread. It may or may not have already been executed when this method
     * returns.
     * 
     * @see ProofNodeSelectionListener#proofNodeSelected(ProofNode)
     * 
     * @param node
     *            the node to be selected
     */
    public void fireSelectedProofNode(final ProofNode node) {
        if(!isFiring) {
            isFiring = true;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (ProofNodeSelectionListener l : listeners) {
                        l.proofNodeSelected(node);
                    }
                    currentProofNode = node;
                    isFiring = false;
                }
            });
        }
    }
    
    /**
     * Indicate that a rule application has been selected.
     * 
     * All registered proof node selection listeners are informed of this
     * selection. The notification is ensured to be run on the swing event queue
     * thread. It may or may not have already been executed when this method
     * returns.
     * 
     * @see ProofNodeSelectionListener#ruleApplicationSelected(RuleApplication)
     * 
     * @param ruleApplication
     *            the rule application to be selected
     */
    public void fireSelectedRuleApplication(final RuleApplication ruleApplication) {
        if(!isFiring) {
            isFiring = true;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (ProofNodeSelectionListener l : listeners) {
                        l.ruleApplicationSelected(ruleApplication);
                    }
                    isFiring = false;
                }
            });
        }
    }
    
    /**
     * handles a term selection in the sequent component.
     * 
     * Setting up the rule application component with all possible rule
     * applications for the situation.
     * 
     * @param sequent
     *            the sequent on which the term appeared.
     * @param termSelector
     *            the reference of the selected term.
     */
    public void termSelected(Sequent sequent, TermSelector termSelector) {

        int goalNo = proof.getOpenGoals().indexOf(currentProofNode);
        if(goalNo == -1) {
            // current sequent is not a goal.
            return;
        }
        
        RuleApplicationFinder iraf = new RuleApplicationFinder(proof, goalNo, env);
        try {
            List<RuleApplication> result = iraf.findAll(termSelector, rulesSortedForInteraction);
            getMainWindow().getRuleApplicationComponent().setInteractiveApplications(result);
        } catch (ProofException e) {
            // TODO gescheiter Report!
            e.printStackTrace();
        }
        
    }

    /**
     * Apply a rule application to the proof.
     * 
     * The call is basically delegated to the proof itself. However, afterwards,
     * in case of a successful application, a sensible node is selected
     * automatically. This is: The target node itself if it is still a goal, or
     * the first child goal of the target of there is any, or the first
     * remaining goal of the entire sequent if there is any or the root node if
     * the proof is closed.
     * 
     * @param ruleApp
     *            the rule application to apply onto the proof.
     * 
     * @throws ProofException
     *             if the application fails.
     */
    public void apply(RuleApplication ruleApp) throws ProofException {
        ProofNode parent = proof.getGoal(ruleApp.getGoalNumber());
        
        proof.apply(ruleApp, env);
        
        // next to select is first child (or self if no children)
        List<ProofNode> children = parent.getChildren();
        ProofNode next;
        if(children == null) {
            // still a goal
            next = parent;
        } else if(children.isEmpty()) {
            if(proof.hasOpenGoals()) {
                // select first open remaining goal
                next = proof.getGoal(0);
            } else {
                next = proof.getRoot();
            }
        } else {
            // select first child goal
            next = children.get(0);
        }
        fireSelectedProofNode(next);
    }
    
    /**
     * Prune a proof.
     * 
     * This is delegated to the proof object. On success, the change of the
     * proof structure is propagated using the
     * {@link #fireSelectedProofNode(ProofNode)} method.
     * 
     * @param proofNode
     *            the node in the proof to prune.
     */
    public void prune(ProofNode proofNode) {
        proof.prune(proofNode);
        fireSelectedProofNode(proofNode);
    }

    /**
     * Gets the currently selected proof node.
     * 
     * @return the currently selected proof node
     */
    public ProofNode getCurrentProofNode() {
        return currentProofNode;
    }

    /**
     * Gets the bar manager of the main window.
     * 
     * The bar manager is responsible for the menu bar and tool bar.
     * 
     * @see BarManager
     * 
     * @return the bar manager
     */
    public BarManager getBarManager() {
        return getMainWindow().getBarManager();
    }

//    /**
//     * Replace the current proof by a new one (e.g. load from a file)
//     * 
//     * The proof must not have children yet.
//     * 
//     * @param proof the new proof
//     */
//    public void replaceProof(@NonNull Proof proof) {
//        assert this.proof.getRoot().getChildren().size() == 0 : 
//            "can only replace childless proof";
//        
//        this.proof = proof;
//        fireSelectedProofNode(proof.getRoot());
//    }

}
