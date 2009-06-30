/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
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
import de.uka.iti.pseudo.term.creation.TermUnification;


// TODO DOC
// Introduce an extra class to match a single rule


/**
 * Objects of this class are used to find applicable rules for 
 * a given term within a sequent.
 */
public class RuleApplicationFinder {
    
    /**
     * Exception used to indicate that enough rule applications have been gathered.
     * It is thrown from within the matching code and caught to stop further matching
     * search.
     */
    private static class EnoughException extends Exception {};
    
    /**
     * The default limit of the number of applicable rules to return.
     */
    private static final int MAX_NUMBER_APPLICATIONS = 20;
    
    /**
     * The sequent under inspection (needed to find assumptions)
     */
    private Sequent sequent;
    
    /**
     * The environment - needed to check where clauses
     */
    private Environment env;
    
    /**
     * Collect applications here.
     */
    private ArrayList<RuleApplication> applications;
    
    /**
     * The currently built rule application.
     */
    private RuleApplicationMaker ruleAppMaker;
    
    /**
     * The goal that we work on.
     */
    private ProofNode goal;
    
    /**
     * and its number
     */
    private int goalNo;

    /**
     * the number of hits after which the search should stop.
     */
    private int stopAtSize;


    /**
     * Instantiates a new interactive rule application finder.
     * 
     * @param proof the proof to inspect
     * @param goalNo the goal no of the inspected node in the proof
     * @param env the environment in the background
     */
    public RuleApplicationFinder(Proof proof, int goalNo, Environment env) {
        this.goal = proof.getGoal(goalNo);
        this.sequent = goal.getSequent();
        this.env = env;
        this.goalNo = goalNo;
    }
        
 //     TODO: Auto-generated Javadoc
    
    
    public RuleApplicationMaker findOne(TermSelector termSelector,  
            List<Rule> sortedAllRules) throws ProofException {
        stopAtSize = 1;
        
        try {
            find(termSelector, sortedAllRules);
            return null;
        } catch (EnoughException e) {
            return ruleAppMaker;
        }
    }
    
    public List<RuleApplication> findAll(TermSelector termSelector,  
            List<Rule> sortedAllRules) throws ProofException {
        stopAtSize = MAX_NUMBER_APPLICATIONS;
        
        try {
            find(termSelector, sortedAllRules);
        } catch (EnoughException e) {
        }
        
        return applications;
    }
    
    /**
     * Find all.
     * 
     * @param termSelector the term selector
     * @param sortedAllRules the sorted all rules
     * 
     * @return the list< rule application>
     * 
     * @throws ProofException the proof exception
     * @throws EnoughException if enough applications have been found
     */
    public void find(TermSelector termSelector,  
            List<Rule> sortedAllRules) throws ProofException, EnoughException {

        applications = new ArrayList<RuleApplication>();
        ruleAppMaker = new RuleApplicationMaker(env);
        ruleAppMaker.setGoalNumber(goalNo);
        
        try {
            for (Rule rule : sortedAllRules) {

                ruleAppMaker.clearProperties();
                ruleAppMaker.setRule(rule);
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

                TermUnification mc = new TermUnification(env);
                ruleAppMaker.setTermUnification(mc);

                if (mc.leftUnify(findClause.getTerm(), termSelector
                        .selectSubterm(sequent)))
                    matchAssumptions(rule.getAssumptions(), mc, 0);

            }
        } catch (RuleException e) {
            throw new ProofException("Error during finding applicable rules", e);
        } 
        
    }

    /**
     * Match assumptions.
     * 
     * @param assumptions the assumptions
     * @param mc the mc
     * @param assIdx the ass idx
     * 
     * @throws RuleException the rule exception
     * @throws EnoughException 
     */
    private void matchAssumptions(List<LocatedTerm> assumptions, TermUnification mc, int assIdx) throws RuleException, EnoughException {
        
        if(assIdx >= assumptions.size()) {
            if(matchWhereClauses(mc)) {
                applications.add(ruleAppMaker.make());
                if (applications.size() >= stopAtSize)
                    throw new EnoughException();
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

    /**
     * Match where clauses.
     * 
     * @param mc the mc
     * 
     * @return true, if successful
     * 
     * @throws RuleException the rule exception
     */
    private boolean matchWhereClauses(TermUnification mc) throws RuleException {
        List<WhereClause> whereClauses = ruleAppMaker.getRule().getWhereClauses();
        for ( WhereClause wc : whereClauses ) {
            if(!wc.applyTo(mc.getTermInstantiator(), ruleAppMaker, goal, env))
                return false;
        }
        return true;
    }
}
