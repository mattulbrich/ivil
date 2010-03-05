/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.ProgramComparingTermInstantiator;
import de.uka.iti.pseudo.term.creation.TermInstantiator;

// TODO DOC

public class Proof extends Observable {
    
    protected ProofNode root;
    
    /**
     * The locking mechanism to ensure synchronisation on the proof
     */
    private Lock lock = new ReentrantLock();
    
    protected List<ProofNode> openGoals = new LinkedList<ProofNode>();

    private boolean changedSinceSave;

    private int proofNodeCounter;
    
    public void apply(@NonNull RuleApplication ruleApp, Environment env) throws ProofException {
        apply(ruleApp, env, null);
    }
    
    public void apply(@NonNull RuleApplication ruleApp, Environment env, Properties whereClauseProperties) throws ProofException {
        ProofNode goal;
        
        lock.lock();
        try {
            Map<String, Term> schemaMap = ruleApp.getSchemaVariableMapping();
            Map<String, Type> typeMap = ruleApp.getTypeVariableMapping();
            Map<String, Update> updateMap = ruleApp.getSchemaUpdateMapping();
            TermInstantiator inst = new ProgramComparingTermInstantiator(schemaMap, typeMap, updateMap, env);

            int goalno = extractGoalNo(ruleApp);
            goal = openGoals.get(goalno);

            goal.apply(ruleApp, inst, env, whereClauseProperties);

            openGoals.remove(goalno);
            openGoals.addAll(goalno, goal.getChildren());

            fireNodeChanged(goal);
            
        } finally {
            lock.unlock();
        }
        
    }
    
    public void prune(ProofNode proofNode) {

        lock.lock();
        try {
            if(proofNode.getProof() != this)
                throw new IllegalArgumentException("The proof node does not belong to me");

            proofNode.prune();

            openGoals.clear();
            root.collectOpenGoals(openGoals);
            
            fireNodeChanged(proofNode);

        } finally {
            lock.unlock();
        }

    }
    
    public Proof(@NonNull Term initialProblem) throws TermException {
        this(new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(initialProblem)));
    }
    
    public Proof(Sequent initialSequent) {
        root = new ProofNode(this, initialSequent, new SequentHistory.Annotation("formula on initial sequent"));
        openGoals.add(root);
    }

    private int extractGoalNo(RuleApplication ruleApp) throws ProofException {
        int goalno = ruleApp.getGoalNumber();
        if(goalno < 0 || goalno >= openGoals.size())
            throw new ProofException("Cannot apply ruleApplication. Illegal goal number in\n" + ruleApp);
        return goalno;
    }
    
    public ProofNode getRoot() {
        return root;
    }

    private void fireNodeChanged(ProofNode proofNode) {
        changedSinceSave = true;
        setChanged();
        notifyObservers(proofNode);
    }
    
    /**
     * notify all observers without argument: They should renew
     * their view on the proof.
     */
    @Override public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

    public List<ProofNode> getOpenGoals() {
        return Collections.unmodifiableList(openGoals);
    }

    public ProofNode getGoal(int goalNo) {
        return openGoals.get(goalNo);
    }

    public boolean hasUnsafedChanges() {
        return changedSinceSave;
    }
    
    public void changesSaved() {
        changedSinceSave = false;
    }

    public boolean hasOpenGoals() {
        return !openGoals.isEmpty();
    }

    int makeFreshNumber() {
        proofNodeCounter ++;
        return proofNodeCounter;
    }

    /**
     * @return the lock
     */
    public Lock getLock() {
        return lock;
    }

    
}
