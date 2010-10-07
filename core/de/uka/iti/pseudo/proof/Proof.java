/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * A proof contains the information on a tree of sequents and their meta
 * information.
 * 
 * It is a mutable object, i.e., the application of inference rules does not
 * result in a different new proof value, but changes the data structures within
 * this object.
 * 
 * <h2>Observable</h2>
 * 
 * All observers which intend to follow changes on this object will receive
 * calls of {@link Observer#update(Observable, Object)} with two kinds of
 * arguments:
 * <ol>
 * <li>Either with a proof node as argument. It is then a node whose set of
 * children has changed recently.
 * <li>Or with an argument of <code>null</code> indicating the some nodes on the
 * tree may have changed.</li>
 * </ol>
 */
public class Proof {

    /**
     * The root node of the proof tree. This will never be null and always
     * contains the sequent to be be proved.
     */
    private @NonNull ProofNode root;


    /**
     * The object which encapsulates the observable part of the pattern. All
     * listeners are added to this observable and all notifications go through
     * it.
     * 
     * This object automatically sets the set changed prior to calling the
     * notification.
     */
    private Observable observable = new Observable() {
        public void notifyObservers(Object arg) {
            setChanged();
            super.notifyObservers(arg);
        };
    };

    /**
     * This list contains all open proof nodes that are reachable from
     * {@link #root}.
     * 
     * This list is synchronized because some GUI elements need concurrent
     * access with automatic proofs.
     */
    private @DeepNonNull List<ProofNode> openGoals = 
        Collections.synchronizedList(new LinkedList<ProofNode>());

    /**
     * A flag which is true iff the proof has been modified after the last call
     * to {@link #hasUnsafedChanges()}
     */
    private boolean changedSinceSave = false;

    /**
     * Proof nodes get unique numbers. It is the job of the proof to enumerate
     * the nodes using this counter.
     */
    private int proofNodeCounter;
    
    /**
     * This mutex is used to ensure apply and prune are atomic operations.
     */
    final Object mutex = new Object();

    /**
     * Instantiates a new proof with an initial sequent.
     * 
     * @param initialSequent
     *            the initial sequent
     */
    public Proof(Sequent initialSequent) {
        root = new ProofNode(this, initialSequent,
                new SequentHistory.Annotation("formula on initial sequent"));
        openGoals.add(root);
    }

    /**
     * Instantiates a new proof with an initial problem term.
     * 
     * <p>
     * The initial sequent used is the sequent which contains nothing but the
     * given formula as succedent.
     * 
     * @param initialProblem
     *            the initial problem
     * 
     * @throws TermException
     *             if the initial problem is not suitable for toplevel usage.
     */
    public Proof(@NonNull Term initialProblem) throws TermException {
        this(new Sequent(Collections.<Term> emptyList(), Collections
                .<Term> singletonList(initialProblem)));
    }

    /**
     * Apply a rule application to this proof. You can only call this from a
     * daemon job.
     * 
     * <p>
     * The number of the goal to apply to is extracted from the application and
     * then the method {@link ProofNode#apply(RuleApplication, Environment)} is
     * invoked.
     * 
     * <p>
     * This method first acquires the lock of the proof before it makes any
     * changes. The lock is released before returning
     * 
     * @param ruleApp
     *            the rule application to apply to this proof.
     * @param env
     *            the environment to which the proof belongs.
     * 
     * @throws ProofException
     *             may be thrown if the application is not successful.
     */
    public void apply(@NonNull RuleApplication ruleApp,
            Environment env)
            throws ProofException {
    
        ProofNode goal;
        
        goal = ruleApp.getProofNode();
        synchronized(mutex){
            int goalno = openGoals.indexOf(goal);
    
            if (goalno == -1) {
                throw new ProofException(
                        "The rule application points to a non-existant or non-goal proof node");
            }
    
            goal.apply(ruleApp, env);
    
            openGoals.remove(goalno);
            openGoals.addAll(goalno, goal.getChildren());
    
            fireNodeChanged(goal);
        }
    }

    /**
     * Gets a goal from the set of open goals by node index.
     * 
     * <code>null</code> is returned if no open goal carries the given number -
     * or an exception thrown.
     * 
     * The resulting proof node belongs to this proof and carries the desired
     * number.
     * 
     * @param nodeNumber
     *            the node number to search for.
     * 
     * @return an open proof node belonging to this proof.
     * 
     * @throws NoSuchElementException
     *             the implementation may choose to throw this is no goal of
     *             this number exists.
     */
    public ProofNode getGoalbyNumber(int nodeNumber) throws NoSuchElementException {
        for (ProofNode goal : openGoals) {
            if(goal.getNumber() == nodeNumber) {
                return goal;
            }
        }
        return null;
    }

    /**
     * Prune all children of a node within this proof.
     * 
     * After the invocation, the proofNode returns <code>null</code> from
     * {@link ProofNode#getChildren()}.
     * 
     * @param proofNode
     *            the proof node
     * 
     * @throws ProofException
     *             if proofNode is not reachable from root, i.e. is not part of
     *             this proof.
     */
    public void prune(ProofNode proofNode) throws ProofException {

        if (proofNode.getProof() != this)
            throw new ProofException("The proof node does not belong to me");

        synchronized (mutex) {
            proofNode.prune();

            openGoals.clear();
            root.collectOpenGoals(openGoals);

            fireNodeChanged(proofNode);
        }
    }

    /**
     * Gets the root of this proof object. It contains the initial sequent.
     * 
     * @return the proof node which is parent of all nodes of the proof.
     */
    public @NonNull ProofNode getRoot() {
        return root;
    }

    /**
     * inform all subscribed observers that a proof node has changed.
     * 
     * <p>
     * The {@link #changedSinceSave} flag is changed to <code>true</code>.
     * 
     * @param proofNode
     *            a proof node whose children have changed recently.
     */
    private void fireNodeChanged(ProofNode proofNode) {
        changedSinceSave = true;
        observable.notifyObservers(proofNode);
    }
    
    

    /**
     * Adds an observer to the set of observers for this proof, provided 
     * that it is not the same as some observer already in the set. 
     * The order in which notifications will be delivered to multiple 
     * observers is not specified. 
     * 
     * @param o an observer to be added.
     * 
     * @see java.util.Observable#addObserver(java.util.Observer)
     */
    public void addObserver(Observer o) {
        observable.addObserver(o);
    }

    /**
     * Deletes an observer from the set of observers of this object. 
     * Passing <CODE>null</CODE> to this method will have no effect.
     * 
     * @param   o   the observer to be deleted.
     * 
     * @see java.util.Observable#deleteObserver(java.util.Observer)
     */
    public void deleteObserver(Observer o) {
        observable.deleteObserver(o);
    }

    /**
     * Gets all open goals, i.e., all proof nodes reachable by {@link #root}
     * which are not yet closed.
     * 
     * @return an unmodifiable list of proof nodes
     */
    public List<ProofNode> getOpenGoals() {
        return Collections.unmodifiableList(openGoals);
    }
    
    /**
     * Checks for open goals on this proof.
     * 
     * @return <code>true</code> if there is still at least one unsolvable goal left, 
     */
    public boolean hasOpenGoals() {
        return !openGoals.isEmpty();
    }

    /**
     * Checks if node can be reached from root. Therefore it walk through
     * parents until root or null is reached.
     * 
     * This method is especially useful to check validity of nodes after pruning
     * occurred.
     * 
     * @param node
     *            the node where the search for root will be started
     * @return true iff root is reachable
     */
    public boolean isReachable(ProofNode node) {
        ProofNode parent = node.getParent();
        while (null != parent) {
            if (parent == root)
                return true;
            else
                parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks for unsaved changes.
     * 
     * @return <code>true</code> iff the proof has been changes since the last
     *         call to {@link #changesSaved()}.
     */
    public boolean hasUnsafedChanges() {
        return changedSinceSave;
    }

    /**
     * clear the flag to remember changes.
     */
    public void changesSaved() {
        changedSinceSave = false;
    }

    /**
     * upon each invocation of this method return a new integer.
     * 
     * @return an integer which has not yet been return for this object
     */
    synchronized int makeFreshNumber() {

        proofNodeCounter++;
        return proofNodeCounter;
    }
}
