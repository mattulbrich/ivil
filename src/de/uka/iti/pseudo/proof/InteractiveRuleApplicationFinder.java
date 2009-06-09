package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO DOC

public class InteractiveRuleApplicationFinder {
    
    private static final int MAX_NUMBER_APPLICATIONS = 20;
    private Sequent sequent;
    private Environment env;
    private ArrayList<RuleApplication> applications;
    private RuleApplicationMaker ruleAppMaker = new RuleApplicationMaker();
    private ProofNode goal;

    public InteractiveRuleApplicationFinder(Proof proof, int goalNo, Environment env) {
        goal = proof.getGoal(goalNo);
        this.sequent = goal.getSequent();
        this.env = env;
        ruleAppMaker.setGoalNumber(goalNo);
    }
    
    public InteractiveRuleApplicationFinder(Sequent sequent, Environment env) {
        this.env = env;
        this.sequent = sequent;
    }

    public List<RuleApplication> findAll(TermSelector termSelector,  
            List<Rule> sortedAllRules) throws ProofException {
        
        applications = new ArrayList<RuleApplication>();
        
        try {
            for (Rule rule : sortedAllRules) {
                if (applications.size() > MAX_NUMBER_APPLICATIONS)
                    break;

                ruleAppMaker.setRule(rule);
                ruleAppMaker.clearProperties();
                ruleAppMaker.setFindSelector(termSelector);

                LocatedTerm findClause = rule.getFindClause();

                if (findClause.getMatchingLocation() == MatchingLocation.ANTECEDENT
                        && (termSelector.isSuccedent() || !termSelector
                                .isToplevel()))
                    continue;

                if (findClause.getMatchingLocation() == MatchingLocation.SUCCEDENT
                        && (termSelector.isAntecedent() || !termSelector
                                .isToplevel()))
                    continue;

                TermUnification mc = new TermUnification();
                ruleAppMaker.setTermUnification(mc);

                if (mc.leftUnify(findClause.getTerm(), termSelector
                        .selectSubterm(sequent)))
                    matchAssumptions(rule.getAssumptions(), mc, 0);

            }
        } catch (RuleException e) {
            throw new ProofException("Error during finding of applicable rules", e);
        }
        
        
        return applications;
    }

    private void matchAssumptions(List<LocatedTerm> assumptions, TermUnification mc, int assIdx) throws RuleException {
        
        if(assIdx >= assumptions.size()) {
            if(matchWhereClauses(mc)) {
                applications.add(ruleAppMaker.make());
            }
            return;
        }
        
        LocatedTerm assumption = assumptions.get(assIdx);
        List<Term> branch;
        boolean isAntecedent = assumption.getMatchingLocation() == MatchingLocation.ANTECEDENT;
        if(isAntecedent) {
            branch = sequent.getAntecedent();
        } else {
            branch = sequent.getSuccedent();
        }
        
        TermUnification mcCopy = mc.clone();
        int termNo = 0;
        for (Term t : branch) {
            if(mc.leftUnify(assumption.getTerm(), t)) {
                ruleAppMaker.pushAssumptionSelector(new TermSelector(isAntecedent, termNo));
                matchAssumptions(assumptions, mc, assIdx+1);
                ruleAppMaker.popAssumptionSelector();
                mc = mcCopy;
                mcCopy = mc.clone();
            }
            termNo++;
        }
    }

    private boolean matchWhereClauses(TermUnification mc) throws RuleException {
        List<WhereClause> whereClauses = ruleAppMaker.getRule().getWhereClauses();
        for ( WhereClause wc : whereClauses ) {
            if(!wc.applyTo(mc.getTermInstantiator(), ruleAppMaker, goal, env))
                return false;
        }
        return true;
    }
}
