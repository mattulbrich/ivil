package de.uka.iti.pseudo.proof;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.uka.iti.pseudo.util.DeferredObservable;

public class Proof {

    protected ProofNode root;
    
    private Observable changeObservable = new DeferredObservable();
    
    protected List<ProofNode> openGoals = new LinkedList<ProofNode>();
    
    public void apply(RuleApplication ruleApp) throws ProofException {
        
        MatchingContext mc = new MatchingContext();
        
        ProofNode goal = extractGoal(ruleApp);
        
        goal.apply(ruleApp, mc);
        
        // TODO: remove goal from list and add children
    }
    
    private ProofNode extractGoal(RuleApplication ruleApp) throws ProofException {
        int goalno = ruleApp.getGoalNumber();
        if(goalno < 0 || goalno >= openGoals.size())
            throw new ProofException("Cannot apply ruleApplication. Illegal goal number in\n" + ruleApp);
        return openGoals.get(goalno);
    }

    public ProofNode getRoot() {
        return root;
    }

    public void addChangeObserver(Observer obs) {
        changeObservable.addObserver(obs);
    }

    public void fireNodeChanged(ProofNode proofNode) {
        changeObservable.notifyObservers(proofNode);
    }

    public ProofNode getGoal(int goalNo) {
        return openGoals.get(goalNo);
    }
    
    public int countOpenGoals() {
        return openGoals.size();
    }
    
}
