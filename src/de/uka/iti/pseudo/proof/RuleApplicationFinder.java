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
    private static final int DEFAULT_NUMBER_APPLICATIONS = 20;
    
    /**
     * The sequent under inspection (needed to find assumptions)
     */
    private Sequent sequent;
    
    /**
     * The environment
     */
    private Environment env;
    
    /**
     * Collect applications here.
     */
    private ArrayList<RuleApplication> applications;
    
    /**
     * The currently built rule application.
     */
    private RuleApplicationMaker ruleAppMaker = new RuleApplicationMaker();
    
    /**
     * The goal that we work on.
     */
    private ProofNode goal;

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
        this(proof, goalNo, env, DEFAULT_NUMBER_APPLICATIONS);
    }
        
    /**
     * Instantiates a new interactive rule application finder.
     * 
     * @param proof the proof to inspect
     * @param goalNo the goal no of the inspected node in the proof
     * @param env the environment in the background
     * @param stopAtSize the number of hits after which the search should stop
     */
    public RuleApplicationFinder(Proof proof, int goalNo, Environment env, int stopAtSize) {
        this.goal = proof.getGoal(goalNo);
        this.sequent = goal.getSequent();
        this.env = env;
        this.stopAtSize = stopAtSize;
        this.ruleAppMaker.setGoalNumber(goalNo);
    }
    
 //     TODO: Auto-generated Javadoc
    
    /**
     * Find all.
     * 
     * @param termSelector the term selector
     * @param sortedAllRules the sorted all rules
     * 
     * @return the list< rule application>
     * 
     * @throws ProofException the proof exception
     */
    public List<RuleApplication> findAll(TermSelector termSelector,  
            List<Rule> sortedAllRules) throws ProofException {

        applications = new ArrayList<RuleApplication>();
        
        try {
            for (Rule rule : sortedAllRules) {

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
        } catch (EnoughException e) {
            // thrown to indicate that no more hits are to be recorded. Return the hits
            // which have been collected so far.
            return applications;
        }
        
        return applications;
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
                if (applications.size() > stopAtSize)
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
