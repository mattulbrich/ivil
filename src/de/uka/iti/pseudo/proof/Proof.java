package de.uka.iti.pseudo.proof;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;
import de.uka.iti.pseudo.util.DeferredObservable;

public class Proof extends Observable {

    protected ProofNode root;
    
    protected List<ProofNode> openGoals = new LinkedList<ProofNode>();
    
    public void apply(RuleApplication ruleApp) throws ProofException {
        
        TermUnification mc = new TermUnification();
        
        ProofNode goal = extractGoal(ruleApp);
        
        goal.apply(ruleApp, mc);
        
        // TODO: remove goal from list and add children
    }
    
    public Proof(Term initialProblem) {
        this(new Sequent(Collections.<Term>emptyList(), Collections.<Term>singletonList(initialProblem)));
    }
    
    public Proof(Sequent initialSequent) {
        root = new ProofNode(this, null, initialSequent);
        openGoals.add(root);
    }

    // needed for mock objects
    protected Proof() { }

    private ProofNode extractGoal(RuleApplication ruleApp) throws ProofException {
        int goalno = ruleApp.getGoalNumber();
        if(goalno < 0 || goalno >= openGoals.size())
            throw new ProofException("Cannot apply ruleApplication. Illegal goal number in\n" + ruleApp);
        return openGoals.get(goalno);
    }

    public ProofNode getRoot() {
        return root;
    }

    public void fireNodeChanged(ProofNode proofNode) {
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
