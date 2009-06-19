package de.uka.iti.pseudo.auto;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;

public class MyStrategy implements Strategy {
    
    private final static String[] REWRITE_CATEGORIES = {
        "close",
        "concrete",
        "updSimpl",
        "prop simp",
        "fol simp",
        "symbex"
    };
    
    private RewriteRuleCollection ruleCollections[];
    
    public MyStrategy(Environment env) throws RuleException {
        ruleCollections = new RewriteRuleCollection[REWRITE_CATEGORIES.length];
        List<Rule> allRules = env.getAllRules();
        for (int i = 0; i < ruleCollections.length; i++) {
            ruleCollections[i] = new RewriteRuleCollection(allRules, REWRITE_CATEGORIES[i], env);
        }
    }
    
    public RuleApplication findRuleApplication(Proof proof) {
        List<ProofNode> openGoals = proof.getOpenGoals();
        for (int i = 0; i < openGoals.size(); i++) {
            RuleApplicationMaker ram = findRuleApplication(proof, i);
            if(ram != null) {
                ram.setGoalNumber(i);
                return ram;
            }
        }
        
        return null;
    }

    private RuleApplicationMaker findRuleApplication(Proof proof, int goalNo) {
        
        for (int i = 0; i < ruleCollections.length; i++) {
            RuleApplicationMaker ruleApplication = ruleCollections[i].findRuleApplication(proof, goalNo);
            if(ruleApplication != null)
                return ruleApplication;
        }

        return null;
    }

    
    
}
