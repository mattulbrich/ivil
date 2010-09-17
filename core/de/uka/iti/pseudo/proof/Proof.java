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
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermInstantiator;

/**
 * A proof contains the information on a tree of sequents and their meta
 * information.
 * 
 * It is a mutable object, i.e., the application of inference rules does not
 * result in a different new proof value, but changes the data structures within
 * this object.
 * 
 * <h2>Locking</h2>
 * 
 * The lock to be used with this object can be obtained using the method
 * {@link #getLock()}. If you intend to do several operations on the proof, take
 * the lock, do your actions and then release it. A typical piece of code would look like:
 * <pre>
 *   Proof p; // ...
 *   
 *   Lock lock = p.getLock();
 *   lock.lock();
 *   try {
 *      RuleApplication ruleApp1; // ... calculate it
 *      RuleApplication ruleApp2; // ... calculate it
 *   
 *      p.apply(ruleApp1);
 *      p.apply(ruleApp2)
 *   } finally {
 *      lock.unlock();
 *   }
 * </pre>
 * 
 * You can also use {@link Lock#tryLock()} and fail in case the lock cannot be acquired.
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
public class Proof extends Observable {

    /**
     * The root node of the proof tree. This will never be null and always
     * contains the sequent to be be proved.
     */
    private @NonNull ProofNode root;

    /**
     * The locking mechanism to ensure synchronisation on the proof.
     */
    private Lock lock = new ReentrantLock();

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
     * Apply a rule application to this proof.
     * 
     * <p>
     * The number of the goal to apply to is extracted from the application and
     * then the method
     * {@link ProofNode#apply(RuleApplication, TermInstantiator, Environment, Properties)}
     * is invoked.
     * 
     * <p>This method first acquires the lock of the proof before it makes any changes.
     * The lock is released before returning 
     * 
     * @param ruleApp
     *            the rule application to apply to this proof.
     * @param env
     *            the environment to which the proof belongs.
     * 
     * @throws ProofException
     *             may be thrown if the application is not successful.
     */
    public void apply(@NonNull RuleApplication ruleApp, Environment env)
            throws ProofException {
    
        ProofNode goal;

        lock.lock();
        
        try {
            goal = ruleApp.getProofNode();            
            int goalno = openGoals.indexOf(goal);
            
            if (goalno == -1) {
                throw new ProofException(
                        "The rule application points to a non-existant or non-goal proof node");
            }
            
            goal.apply(ruleApp, env);

            openGoals.remove(goalno);
            openGoals.addAll(goalno, goal.getChildren());

            fireNodeChanged(goal);

        } finally {
            lock.unlock();
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

        lock.lock();
        try {
            if (proofNode.getProof() != this)
                throw new ProofException(
                        "The proof node does not belong to me");

            proofNode.prune();

            openGoals.clear();
            root.collectOpenGoals(openGoals);

            fireNodeChanged(proofNode);

        } finally {
            lock.unlock();
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
     * inform all subscribed ovservers that a proof node has changed.
     * 
     * <p>
     * The {@link #changedSinceSave} flag is changed to <code>true</code>.
     * 
     * @param proofNode
     *            a proof node whose children have changed recently.
     */
    private void fireNodeChanged(ProofNode proofNode) {
        changedSinceSave = true;
        setChanged();
        notifyObservers(proofNode);
    }

    /**
     * notify all observers without argument: They should renew their perception of
     * the proof.
     */
    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
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
     * Gets a particular goal of the list of open goals.
     * 
     * <p>
     * Since the structure of a proof may change rapidly, you should first
     * acquire the proof's lock ({@link #getLock()}) and then do inquiries on
     * its content, e.g., by using this method.
     * 
     * @param goalNo
     *            the number of the goal to retrieve
     * 
     * @return the goal which corresponds to tje open problem.
     * 
     * @throws IndexOutOfBoundsException
     *             if the argument is outside the range of valid indices of
     *             {@link #openGoals}.
     * 
     */
//    public @NonNull ProofNode getGoal(int goalNo) {
//        return openGoals.get(goalNo);
//    }

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

    /**
     * Get the lock that protects this proof object from being tampered with.
     * 
     * <p>
     * If you intend to have a consistent state of the proof and want to apply
     * rules, be sure to have acquired this lock beforehand.
     * 
     * @return the write lock for this proof
     */
    public @NonNull Lock getLock() {
        return lock;
    }

}
