package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

public class InteractiveRuleApplicationFinder {
    
    private static final int MAX_NUMBER_APPLICATIONS = 20;
    private Sequent sequent;
    private Environment env;
    private ArrayList<RuleApplication> applications;
    private RuleApplicationMaker ruleAppMaker = new RuleApplicationMaker();

    public InteractiveRuleApplicationFinder(Proof proof, int goalNo, Environment env) {
        this.sequent = proof.getGoal(goalNo).getSequent();
        this.env = env;
        ruleAppMaker.setGoalNumber(goalNo);
    }

    public List<RuleApplication> findAll(TermSelector termSelector,  
            List<Rule> sortedAllRules) throws ProofException {
        
        applications = new ArrayList<RuleApplication>();
        
        for (Rule rule : sortedAllRules) {
            if(applications.size() > MAX_NUMBER_APPLICATIONS)
                break;
            
            ruleAppMaker.setRule(rule);
            ruleAppMaker.setFindSelector(termSelector);
            
            LocatedTerm findClause = rule.getFindClause();
            
            if(findClause.getMatchingLocation() == MatchingLocation.ANTECEDENT 
                    && (termSelector.isSuccedent() || !termSelector.isToplevel()))
                continue;
            
            if(findClause.getMatchingLocation() == MatchingLocation.SUCCEDENT 
                    && (termSelector.isAntecedent() || !termSelector.isToplevel()))
                continue;
            
            
            MatchingContext mc = new MatchingContext();
            
            if(mc.leftMatch(findClause.getTerm(), termSelector.selectSubterm(sequent))) {
                matchAssumptions(rule.getAssumptions(), mc, 0);
            } 
            
        }
        return null;
    }

    private void matchAssumptions(LocatedTerm[] assumptions, MatchingContext mc, int assIdx) {
        
        if(assIdx >= assumptions.length) {
            applications.add(ruleAppMaker.make());
            return;
        }
        
        LocatedTerm assumption = assumptions[assIdx];
        List<Term> branch;
        boolean isAntecedent = assumption.getMatchingLocation() == MatchingLocation.ANTECEDENT;
        if(isAntecedent) {
            branch = sequent.getAntecedent();
        } else {
            branch = sequent.getSuccedent();
        }
        
        MatchingContext mcCopy = mc.clone();
        int termNo = 0;
        for (Term t : branch) {
            if(mc.leftMatch(assumption.getTerm(), t)) {
                ruleAppMaker.pushAssumptionSelector(new TermSelector(isAntecedent, termNo));
                matchAssumptions(assumptions, mc, assIdx+1);
                ruleAppMaker.popAssumptionSelector();
                mc = mcCopy;
                mcCopy = mc.clone();
            }
            termNo ++;
        }
    }
}
