package de.uka.iti.pseudo.proof;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;

public class Proof {

    private ProofNode root;
    
    private List<ProofNode> openGoals = new LinkedList<ProofNode>();
    
    public void apply(RuleApplication ruleApp) throws ProofException {
        
        MatchingContext mc = new MatchingContext();
        
        Rule rule = ruleApp.getRule();
        ProofNode goal = extractGoal(ruleApp);
        
        matchFindClause(ruleApp, mc, rule, goal);
        matchAssumeClauses(ruleApp, mc, rule, goal);
        
        for (WhereClause whereClause : rule.getWhereClauses()) {
            if(!whereClause.applyTo(mc, ruleApp, goal))
                throw new ProofException("WhereClause not true : " + whereClause);
        }
        
        
    }

    private void matchFindClause(RuleApplication ruleApp, MatchingContext mc,
            Rule rule, ProofNode goal) throws ProofException {
        TermSelector findSelector = ruleApp.getFindSelector();
        Term findSubTerm = goal.selectSubterm(findSelector);

        LocatedTerm findClause = rule.getFindClause();
        if(!findClause.isFittingSelect(findSelector)) {
            throw new ProofException("Illegal selector for find");
        }
        mc.leftMatch(findClause.getTerm(), findSubTerm);
    }

    private void matchAssumeClauses(RuleApplication ruleApp,
            MatchingContext mc, Rule rule, ProofNode goal)
            throws ProofException {
        int length = ruleApp.getAssumeSelectors().length;
        TermSelector[] assumeSelectors = ruleApp.getAssumeSelectors();

        assert length == assumeSelectors.length;

        for(int i = 0; i < length; i++) {
            assert !assumeSelectors[i].hasSubtermNo();
            Term assumeTerm = goal.selectTerm(assumeSelectors[i]);
            LocatedTerm assumption = rule.getAssumptions()[i];
            if(!assumption.isFittingSelect(assumeSelectors[i])) {
                throw new ProofException("Illegal selector for assume (" + i + ")");
            }
            mc.leftMatch(assumption.getTerm(), assumeTerm);
        }
    }
    
    private ProofNode extractGoal(RuleApplication ruleApp) throws ProofException {
        int goalno = ruleApp.getGoalNumber();
        if(goalno < 0 || goalno >= openGoals.size())
            throw new ProofException("Cannot apply ruleApplication. Illegal goal number in\n" + ruleApp);
        return openGoals.get(goalno);
    }
    
}
