package de.uka.iti.pseudo.proof;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO DOC

public class Proof extends Observable {

    protected ProofNode root;
    
    protected List<ProofNode> openGoals = new LinkedList<ProofNode>();
    
    public synchronized void apply(@NonNull RuleApplication ruleApp, Environment env) throws ProofException {
        
        TermUnification mc = new TermUnification();
        
        int goalno = extractGoalNo(ruleApp);
        ProofNode goal = openGoals.get(goalno);
        
        goal.apply(ruleApp, mc, env);
        
        openGoals.remove(goalno);
        openGoals.addAll(goalno, goal.getChildren());
        fireNodeChanged(goal);        
    }
    
    public Proof(Term initialProblem) throws TermException {
        this(new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(initialProblem)));
    }
    
    public Proof(Sequent initialSequent) {
        root = new ProofNode(this, null, initialSequent);
        openGoals.add(root);
    }

    // needed for mock objects
    protected Proof() { }

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
        setChanged();
        notifyObservers(proofNode);
    }

    public List<ProofNode> getOpenGoals() {
        return Collections.unmodifiableList(openGoals);
    }

    public ProofNode getGoal(int goalNo) {
        return openGoals.get(goalNo);
    }
    
}
