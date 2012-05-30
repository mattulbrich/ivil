/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.ArrayList;
import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMatcher;

// Introduce an extra class to match a single rule (??)

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
    @SuppressWarnings("serial") 
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
     * the number of hits after which the search should stop.
     */
    private int stopAtSize;

    /**
     * A filter to rule applications. It is originally set to null but may be
     * changed. Rule applications are only added to {@link #applications} if
     * this filter accepts them (or is null).
     */
    private RuleApplicationFilter applicationFilter;

    /**
     * Instantiates a new interactive rule application finder.
     * 
     * @param proof
     *            the proof to inspect
     * @param node
     *            the inspected node in the proof
     * @param env
     *            the environment in the background
     */
    public RuleApplicationFinder(Proof proof, ProofNode node, Environment env) {
        this.goal = node;
        assert goal != null;
        assert goal.getChildren() == null : "Must not have children";
        this.sequent = goal.getSequent();
        this.env = env;
    }

    /**
     * Find one single rule application on a subterm in the sequent under
     * inspection.
     * 
     * @param termSelector
     *            the term selector for the subterm to match with find
     * @param rules
     *            the set of rules to check
     * 
     * @return the rule application containing the found match
     * 
     * @throws ProofException
     *             may be thrown during the search of applicable rules
     */
    public @Nullable RuleApplicationMaker findOne(TermSelector termSelector,  
            List<Rule> rules) throws ProofException {
        stopAtSize = 1;

        try {
            find(termSelector, rules);
            return null;
        } catch (EnoughException e) {
            return ruleAppMaker;
        }
    }

    /**
     * Find all applicable rule applications on a subterm in the sequent under
     * inspection.
     * 
     * @param termSelector
     *            the term selector for the subterm to match with find
     * @param rules
     *            the set of rules to check
     * 
     * @return a list of rule applications. may be empty if no applications could be found.
     * 
     * @throws ProofException
     *             may be thrown during the search of applicable rules
     */
    public List<RuleApplication> findAll(TermSelector termSelector,  
            List<Rule> rules) throws ProofException {
        stopAtSize = MAX_NUMBER_APPLICATIONS;

        try {
            find(termSelector, rules);
        } catch (EnoughException e) {
            // the desired number of apps has been found, indicated by this
            // exception. We can return now. There has not been an error.
        }

        return applications;
    }

    /**
     * Gets the currently installed application filter.
     * 
     * @return the application filter or null
     */
    public RuleApplicationFilter getApplicationFilter() {
        return applicationFilter;
    }

    /**
     * Sets the application filter to be used in the future.
     * Setting it to null turns filtering off.
     * 
     * @param applicationFilter
     *            the new application filter or null
     */
    public void setApplicationFilter(RuleApplicationFilter applicationFilter) {
        this.applicationFilter = applicationFilter;
    }
    
    /**
     * Does the actual searching. goes over all possibilities and stores
     * applicable ones in applications. When the number of desired items is
     * found, throw an {@link EnoughException}.
     * 
     * @param termSelector
     *            the term to be selected from the sequent
     * @param sortedAllRules
     *            the set of rules to check
     * 
     * @throws ProofException
     *             the proof fails
     * @throws EnoughException
     *             if enough applications have been found
     */
    private void find(TermSelector termSelector,  
            List<Rule> sortedAllRules) throws ProofException, EnoughException {

        applications = new ArrayList<RuleApplication>();
        ruleAppMaker = new RuleApplicationMaker(env);
        ruleAppMaker.setProofNode(goal);
        
        try {
            for (Rule rule : sortedAllRules) {

                ruleAppMaker.clearProperties();
                ruleAppMaker.setRule(rule);
                ruleAppMaker.setFindSelector(termSelector);
                ruleAppMaker.getTermMatcher().clear();

                LocatedTerm findClause = rule.getFindClause();
                TermMatcher termMatcher = ruleAppMaker.getTermMatcher();
                
                if(findClause != null) {
                    if (findClause.getMatchingLocation() == MatchingLocation.ANTECEDENT
                            && (termSelector.isSuccedent() || !termSelector
                                    .isToplevel()))
                        continue;

                    if (findClause.getMatchingLocation() == MatchingLocation.SUCCEDENT
                            && (termSelector.isAntecedent() || !termSelector
                                    .isToplevel()))
                        continue;


                    if (!termMatcher.leftMatch(findClause.getTerm(), termSelector
                            .selectSubterm(sequent)))
                        continue;
                }

                matchAssumptions(rule.getAssumptions(), 0);

            }
        } catch (RuleException e) {
            throw new ProofException("Error during finding applicable rules", e);
        } 
        
    }

    /**
     * Match assumptions recursively.
     * 
     * Try all possible assumption instantiation. If no more assumptions are to
     * be matched and the where clauses can be checked add a copy of the current
     * {@link #ruleAppMaker} to the set {@link #applications}.
     * 
     * Applications are only added if the optionally defined filter accepts
     * them.
     * 
     * @param assumptions
     *            the list of assumptions
     * @param assIdx
     *            the index of the current assumption within assumptions
     * 
     * @throws RuleException
     *             may appear when checking where clauses
     * @throws EnoughException if enough applications have been found
     */
    private void matchAssumptions(List<LocatedTerm> assumptions,
            int assIdx) throws RuleException,
            EnoughException {

        TermMatcher mc = ruleAppMaker.getTermMatcher();
        if (assIdx >= assumptions.size()) {
            if (matchWhereClauses(mc)) {
                RuleApplication rap = ruleAppMaker.make();
                if(applicationFilter == null || applicationFilter.accepts(rap)) {
                    applications.add(rap);
                    if (applications.size() >= stopAtSize)
                        throw new EnoughException();
                }
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
        
        
        TermMatcher mcCopy = mc.clone();
        int termNo = 0;
        for (Term t : branch) {
            if(mc.leftMatch(assumption.getTerm(), t)) {
                ruleAppMaker.pushAssumptionSelector(new TermSelector(isAntecedent, termNo)); //ok
                matchAssumptions(assumptions, assIdx+1);
                ruleAppMaker.popAssumptionSelector();
                mc = mcCopy.clone();
                ruleAppMaker.setTermMatcher(mc);
            }
            termNo++;
        }
    }

    /**
     * Match where clauses one after the other. All of them have to return true.
     * 
     * Before validating the clauses, the conditions are given a
     * chance to instantiate schema entities if needed. Active instantiation is
     * performed before starting checking, hence, an instantiation cannot
     * invalidate a check.
     * 
     * @param mc
     *            the unification context.
     * 
     * @return true, if successful
     * 
     * @throws RuleException
     *             may be thrown by the where condition
     */
    private boolean matchWhereClauses(TermMatcher mc) throws RuleException {
        
        List<WhereClause> whereClauses = ruleAppMaker.getRule().getWhereClauses();
        
        for (WhereClause wc : whereClauses) {
            wc.addInstantiations(mc, ruleAppMaker, env);
        }
        
        for (WhereClause wc : whereClauses) {
            if (!wc.applyTo(mc.getTermInstantiator(), ruleAppMaker, env))
                return false;
        }
        return true;
    }

}
