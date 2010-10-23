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
package de.uka.iti.pseudo.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.auto.strategy.BreakpointStrategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.RulePriorityComparator;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.NotificationSupport;
import de.uka.iti.pseudo.util.Util;

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
 * {@link #getApplicableRules(Sequent, TermSelector)}.
 * 
 * A general PropertyChange mechanism is installed to provide an opportunity to
 * work with general properties on the proof process. 
 */
public class ProofCenter implements Observer {

    /**
     * Property key indicating that an automatic proof is on the run. This will
     * be set by all actions in actions.auto. <br>
     * Type: Boolean
     */
    public static final String ONGOING_PROOF = "pseudo.ongoing_proof";
    
    /**
     * Property key to indicate that a proof node has been selected.
     * Type: ProofNode
     */
    public static final String SELECTED_PROOFNODE = "pseudo.selectedProofNode";
    
    /**
     * Property key to indicate that a rule application has been selected.
     * Type: RuleApplication
     */
    public static final String SELECTED_RULEAPPLICATION = "pseudo.selectedRuleApplication";
    
    /**
     * Property key to denote the verbosity of the display
     * Type: int 
     */
    public static final String TREE_VERBOSITY = "pseudo.tree.verbosity";
    
    /**
     * Property key to denote whether numbers should be printed in display
     * Type: boolean
     */
    public static final String TREE_SHOW_NUMBERS = "pseudo.tree.shownumbers";

    /**
     * Notification signal to indicate that a node in the proof has been changed.
     * Activated every time that the proof is changed (observing the proof)
     */
    public static final String PROOFNODE_HAS_CHANGED = "pseudo.proofnode_changed";
    
    /**
     * Notification signal to indicate that the proof has changed.
     * This is called after an action on the proof has finished. This notification
     * may come after 0, 1 or several proof node changes to the proof. 
     */
    public static final String PROOFTREE_HAS_CHANGED = "pseudo.prooftree_changed";
    
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
     * The strategy manager which is used throughout this proof for
     * automated deduction
     */
    private StrategyManager strategyManager;
    
    /**
     * All rules of the environment, sorted for interaction.
     * (at the moment not sorted at all)
     */
    private List<Rule> rulesSortedForInteraction;
    
    /**
     * the system pretty printer used by components, 
     * configured by menu
     */
    private PrettyPrint prettyPrinter;
    
    /**
     * general property mechnism to allow listening w/o declaration of new
     * elements here. This is the listener support
     */
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    /**
     * general property mechnism to allow listening w/o declaration of new
     * elements here.This is the value support.
     */
    private Map<String, Object> generalProperties = new HashMap<String, Object>();

    /**
     * general notification mechanism to allow for listening to events.
     */
    private NotificationSupport notificationSupport = new NotificationSupport(this);
    
    /**
     * Instantiates a new proof center.
     * 
     * Creates a new pretty pringer, a new strategy manager, a new main window,
     * ...
     * 
     * @param proof
     *            the proof to use
     * @param env
     *            the environment
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred. This happens if
     *             the {@link BarManager} cannot find resources
     * @throws StrategyException
     *             signals that the strategy manager could not be properly
     *             initialised.
     */
    public ProofCenter(@NonNull Proof proof, @NonNull Environment env)  throws IOException, StrategyException {
        this.proof = proof;
        this.env = env;
        this.prettyPrinter = new PrettyPrint(env);
        
        this.strategyManager = new StrategyManager(proof, env);
        this.strategyManager.registerAllKnownStrategies();
        
        proof.addObserver(this);

        firePropertyChange(ONGOING_PROOF, false);
        
        mainWindow = new MainWindow(this, env.getResourceName());
        mainWindow.makeGUI();
        fireSelectedProofNode(proof.getRoot());
        
        prepareRuleLists();
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
            if(rule.getProperty(RuleTagConstants.KEY_AUTOONLY) != null)
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
    public @NonNull
    Environment getEnvironment() {
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
     * Indicate that a proof node has been selected.
     * 
     * All registered proof node selection listeners are informed of this
     * selection. The notification is ensured to be run on the swing event queue
     * thread. It may or may not have already been executed when this method
     * returns.
     * 
     * This method will fire a change, even if the same node has been selected,
     * so listeners must not invoke this method.
     * 
     * @param node
     *            the node to be selected
     */
    public void fireSelectedProofNode(@NonNull ProofNode node) {
        firePropertyChange(SELECTED_PROOFNODE, node);
    }

    /**
     * Indicate that a proof step has been completed and the tree should be
     * reassessed.
     * 
     * All registered notification listeners listening to the signal
     * {@link ProofCenter#PROOFTREE_HAS_CHANGED} will get notified.
     * 
     * @param selectNextGoal
     *            if this is true, the next selectable goal is automatically
     *            selected.
     */
    public void fireProoftreeChangedNotification(final boolean selectNextGoal) {
        fireNotification(PROOFTREE_HAS_CHANGED);
        if(selectNextGoal) {
            selectNextGoal();
        }
    }
    
    /**
     * Gets the List of possible rule applications for a term within the
     * currently selected proof node. The term is given by its selector.
     * 
     * <P>
     * A list of all possible rule applications which match against a rule which
     * is not marked automatic-only.
     * 
     * @param termSelector
     *            the reference of the selected term.
     *            
     * @return a list of rule applications that match the selected term
     * 
     * @throws ProofException
     */
    public @NonNull List<RuleApplication> getApplicableRules(
            @NonNull TermSelector termSelector) throws ProofException {
        
        Log.enter(termSelector);

        ProofNode node = getCurrentProofNode();
        RuleApplicationFinder iraf = new RuleApplicationFinder(proof, node, env);
        List<RuleApplication> result = iraf.findAll(termSelector, rulesSortedForInteraction);

        Log.log(Log.VERBOSE, "Found rule apps: " + result);
        return result;
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
     * <p>
     * The method returns the next open goal. If the application created open
     * nodes, the first one will be returned. If the application closed the
     * branch, the first open goal will be returned. If the whole tree is
     * closed, the root is returned - though not an open goal.
     * 
     * @param ruleApp
     *            the rule application to apply onto the proof.
     *            
     * @throws ProofException
     *             if the application fails.
     */
    public void apply(RuleApplication ruleApp) throws ProofException {
        proof.apply(ruleApp, env);
    }

    /**
     * Prune a proof.
     * 
     * This is delegated to the proof object. On success, the change of the
     * proof structure is propagated by a notification of the signal
     * {@value #PROOFTREE_HAS_CHANGED}.
     * 
     * @param proofNode
     *            the node in the proof to prune.
     * @throws ProofException
     *             if the node is not part of this proof.
     */
    public void prune(ProofNode proofNode) throws ProofException {
        proof.prune(proofNode);
    }

    /**
     * Gets the currently selected proof node.
     * 
     * @return the currently selected proof node
     */
    public @Nullable ProofNode getCurrentProofNode() {
        Object currentPN = getProperty(SELECTED_PROOFNODE);
        return (currentPN instanceof ProofNode) ? (ProofNode)currentPN : null;   
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

    /**
     * Get the pretty printer for this proof surrounding. The printer can be
     * changed via menu entries. You can add a {@link PropertyChangeListener} if
     * you want to be informed about changes.
     * 
     * @return the system pretty printer;
     */
    public PrettyPrint getPrettyPrinter() {
        return prettyPrinter;
    }

    /**
     * Get the strategy manager for this proof surrounding.
     *  
     * @return the system strategy manager 
     */
    public @NonNull StrategyManager getStrategyManager() {
        return strategyManager;
    }
    
    /**
     * get the BreakpointManager for the surrounding.
     * 
     * The BreakpointManager is not necessarily part of the system.
     * This will fail if {@link BreakpointStrategy} is not available since
     * this class is asked to provide that instance.
     * 
     * <p>We cannot keep the instance here because the strategies are part
     * of the core system and cannot access proof centers.
     * 
     * @return the breakpoint manager of the {@link BreakpointStrategy}.
     * @see BreakpointStrategy#getBreakpointManager() 
     */
    public BreakpointManager getBreakpointManager() {
        return getStrategyManager().getStrategy(BreakpointStrategy.class)
                .getBreakpointManager();
    }

    /**
     * react to changes on the proof ... delegate to the UI components (on UI
     * thread)
     */
    @Override
    public void update(Observable o, Object arg) {
        
        final ProofNode pn = (ProofNode) arg;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireNotification(PROOFNODE_HAS_CHANGED, pn);
            }
        });
    }
    
    // Delegations to changeSupport!
    
    /**
     * Adds a listener loooking for a certain kind of changes.
     * 
     * @see PropertyChangeSupport#addPropertyChangeListener(String,
     *      PropertyChangeListener)
     * 
     * @param propertyName
     *            the property to look out for
     * @param listener
     *            the listener to handle changes
     */
    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }
    
    public void addNotificationListener(String signal, NotificationListener listener) {
        notificationSupport.addNotificationListener(signal, listener);
    }

    /**
     * Notify all registered listeners that a property's value has changed.
     * 
     * <p>
     * Please note that an event is only triggered if the new value differs from
     * the originally set value.
     * 
     * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
     * 
     * @param propertyName
     *            name of the property
     * @param newValue
     *            value after the change.
     */
    public void firePropertyChange(String propertyName, Object newValue) {
        // FIXME decide whether or not to remove this assertion, as it can't be
        // used together with unit tests
        // assert SwingUtilities.isEventDispatchThread();
        Object oldValue = generalProperties.get(propertyName);
        Log.log("Changing " + propertyName + " from " + oldValue + " to " + newValue);
        generalProperties.put(propertyName, newValue);
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Notify all registered listeners that a property's value has been set.
     * 
     * <p>
     * Please note that an event is triggered even if no <b>new</b> value is set
     * but rather the old value repeated. Setting a value of <code>null</code>
     * however, does not trigger an event.
     * 
     * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
     * 
     * @param propertyName
     *            name of the property
     * @param newValue
     *            value after the change.
     */
    public void fireNotification(String signal, Object... parameters) {
        assert SwingUtilities.isEventDispatchThread();
        Log.enter(signal, Util.readOnlyArrayList(parameters));
        notificationSupport.fireNotification(signal, parameters);
    }
    
    /**
     * Gets the value of a property.
     * 
     * @param propertyName
     *            the property name
     * 
     * @return the property's value. null if not set
     */
    public Object getProperty(String propertyName) {
        return generalProperties.get(propertyName);
    }
    
    /**
     * Removes a listener loooking for a certain kind of changes.
     * 
     * @see PropertyChangeSupport#removePropertyChangeListener(String,
     *      PropertyChangeListener)
     * 
     * @param propertyName
     *            the property to look out for
     * @param listener
     *            the listener to handle changes
     */
    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }
    
    public void removeNotificationListener(String signal, NotificationListener listener) {
        notificationSupport.removeNotificationListener(signal, listener);
    }
    
    /**
     * This method prints all registered {@link PropertyChangeListener}s 
     * to System.err. It is solely for debug purposes.
     */
    public void dumpPropertyListeners() {
        PropertyChangeListener[] listeners = changeSupport
                .getPropertyChangeListeners();
        for (PropertyChangeListener listener : listeners) {
            if (listener instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
                System.err.println(proxy.getPropertyName() + ": "
                        + proxy.getListener());
            } else {
                System.err.println("*: " + listener);
            }
        }

    }

    private void selectNextGoal() {
        ProofNode res = findGoal(getCurrentProofNode());
        
        if(res == null) {
            res = findGoal(proof.getRoot());
        }
        
        if(res == null) {
            Log.log(Log.DEBUG, "No goal to select, selected root");
            fireSelectedProofNode(proof.getRoot());
        } else {
            Log.log(Log.DEBUG, "Goal selected: " + res);
            fireSelectedProofNode(res);
        }
    }
    
    private ProofNode findGoal(ProofNode p) {
        if(p.getChildren() == null)
            return p;
        
        for (ProofNode pn : p.getChildren()) {
            ProofNode res = findGoal(pn);
            if(res != null)
                return res;
        }
        
        return null;
    }

}
