package de.uka.iti.pseudo.proof;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class Proof extends Observable {

    private ProofNode root;
    
    private List<ProofNode> openGoals = new LinkedList<ProofNode>();
    
    public void apply(RuleApplication ruleApp) throws ProofException {
        
        MatchingContext mc = new MatchingContext();
        
        ProofNode goal = extractGoal(ruleApp);
        
        goal.apply(ruleApp, mc);
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
    
}
