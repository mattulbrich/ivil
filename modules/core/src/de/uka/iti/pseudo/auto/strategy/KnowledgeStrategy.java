/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nonnull.DeepNonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;

/**
 * The Knowledge Strategy can be used as a specialised simplification strategy for the three rules:
 * <ol><li>replace_known_right
 * <li>replace_known_left
 * <li>equality_apply
 * </ol>
 * 
 * Since these rules match against every subterm and have a generic assumption, searching application possibilities 
 * for them using the usual matching strategies would result in a slowdown of factor 10. 
 */
public class KnowledgeStrategy extends AbstractStrategy {

    /**
     * The "replace_known_right" rule. Is null if this rule is not available in
     * the environment.
     */
    private @Nullable Rule ruleReplaceKnownRight;
    
    /**
     * The rule "replace_known_left". Is null if this rule is not available in
     * the environment.
     */
    private @Nullable Rule ruleReplaceKnownLeft;
    
    /**
     * The rule "apply_equality". Is null if this rule is not available in
     * the environment.
     */
    private @Nullable Rule ruleApplyEquality;
    
    /**
     * The environment in which the strategy works.
     */
    private Environment env;

    /**
     * Translates a list of Integer objects to an array of primitive int-values.
     * 
     * @param list
     *            a list
     * 
     * @return a freshly created array of same length
     * 
     * @throws NullPointerException if the list contains a null element.
     */
    private static int[] toIntArray(@DeepNonNull List<Integer> list) {
        int[] result = new int[list.size()];
        int i = 0;
        for (Integer val : list) {
            result[i++] = val;
        }
        
        return result;
    }
    
    

    /**
     * Tests if the argument is an equality.
     * (An application of the function "$eq").
     * 
     * @param term
     *            the term
     * 
     * @return true, iff term is an equality
     */
    private static boolean isEquality(Term term) {
        if (term instanceof Application) {
            Application application = (Application) term;
            Function fct = application.getFunction();
            return "$eq".equals(fct.getName());
        }
        return false;
    }

    /**
     * The Class Detector does the actual work for one search.
     * 
     * It has been introduced because the strategy can be invoked several times
     * from different threads. Each invocation creates its own detector then to
     * store data relevant for the search.
     */
    private class Detector {

        /**
         * The proof node to work on.
         */
        private ProofNode target;
        
        /**
         * A map from all terms in the antecedent to their term selector. 
         */
        private Map<Term,TermSelector> antecedentMap;
        
        /**
         * A map from all terms in the succedent to their term selector.
         */
        private Map<Term,TermSelector> succedentMap;
        
        /**
         * A map from all left-hand-sides of equalitites terms in the antecedent
         * to their right-hand-sides.
         */
        private Map<Term,TermSelector> antecedentEqualities;
        
        /**
         * The antecedent.
         */
        private List<Term> antecedent;
        
        /**
         * The succedent.
         */
        private List<Term> succedent;
        
        /**
         * A dynamically modified list models the current path.
         */
        private LinkedList<Integer> path = new LinkedList<Integer>();

        /**
         * Instantiates a new detector.
         * 
         * @param target
         *            the target
         */
        public Detector(ProofNode target) {
            this.target = target;
            
            Sequent sequent = target.getSequent();
            antecedent = sequent.getAntecedent();
            succedent = sequent.getSuccedent();
            
            // hash sets are way more efficient for contains-checking than lists.
            antecedentMap = new HashMap<Term, TermSelector>();
            for (int i = 0; i < antecedent.size(); i++) {
                antecedentMap.put(antecedent.get(i), new TermSelector(TermSelector.ANTECEDENT, i));
            }
            
            succedentMap = new HashMap<Term, TermSelector>();
            for (int i = 0; i < succedent.size(); i++) {
                succedentMap.put(succedent.get(i), new TermSelector(TermSelector.SUCCEDENT, i));
            }
            
            // store all equalities in the antecedent
            antecedentEqualities = new HashMap<Term, TermSelector>();
            int index = 0;
            for (Term term : antecedent) {
                if(isEquality(term)) {
                    antecedentEqualities.put(term.getSubterm(0), new TermSelector(TermSelector.ANTECEDENT, index));
                }
                index ++;
            }
        }

        /**
         * Starts the actual detection
         * 
         * @return a rule application if found, null otherwise
         * 
         * @throws ProofException
         *             if matching does not work.
         */
        private @Nullable RuleApplication detect() throws ProofException {

            path.clear();
            
            for (int i = 0; i < antecedent.size(); i++) {
                assert path.isEmpty();
                TermSelector top = new TermSelector(TermSelector.ANTECEDENT, i);
                RuleApplication ra = findRA(top, antecedent.get(i));
                if(ra != null) {
                    return ra;
                }
            }
            
            for (int i = 0; i < succedent.size(); i++) {
                assert path.isEmpty();
                TermSelector top = new TermSelector(TermSelector.SUCCEDENT, i);
                RuleApplication ra = findRA(top, succedent.get(i));
                if(ra != null) {
                    return ra;
                }
            }
            
            return null;
        }

        private @Nullable RuleApplication findRA(TermSelector toplevelSelector, Term term) throws ProofException {
            TermSelector refind = antecedentMap.get(term);
            if(refind != null && ruleReplaceKnownLeft != null) {
                TermSelector find = new TermSelector(toplevelSelector, toIntArray(path));
                if(!find.equals(refind)) {
                    RuleApplicationMaker ra = new RuleApplicationMaker(env);
                    ra.setProofNode(target);
                    ra.setFindSelector(new TermSelector(toplevelSelector, toIntArray(path)));
                    ra.pushAssumptionSelector(refind);
                    ra.setFindSelector(find);
                    assert ruleReplaceKnownLeft != null : "nullness conviction, still not null";
                    ra.setRule(ruleReplaceKnownLeft);
                    ra.matchInstantiations();
                    return ra;
                }
            }
            
            refind = succedentMap.get(term);
            if(refind != null && ruleReplaceKnownRight != null) {
                TermSelector find = new TermSelector(toplevelSelector, toIntArray(path));
                if(!find.equals(refind)) {
                    RuleApplicationMaker ra = new RuleApplicationMaker(env);
                    ra.setProofNode(target);
                    ra.setFindSelector(new TermSelector(toplevelSelector, toIntArray(path)));
                    ra.getAssumeSelectors().add(refind);
                    ra.setFindSelector(find);
                    assert ruleReplaceKnownRight != null : "nullness conviction, still not null";
                    ra.setRule(ruleReplaceKnownRight);
                    ra.matchInstantiations();
                    return ra;
                }
            }
                
            refind = antecedentEqualities.get(term);
            if(refind != null && ruleApplyEquality != null) {
                TermSelector find = new TermSelector(toplevelSelector, toIntArray(path));
                if(!find.hasPrefix(refind)) {
                    RuleApplicationMaker ra = new RuleApplicationMaker(env);
                    ra.setProofNode(target);
                    ra.setFindSelector(find);
                    ra.getAssumeSelectors().add(refind);
                    assert ruleApplyEquality != null : "nullness conviction, still not null";
                    ra.setRule(ruleApplyEquality);
                    ra.matchInstantiations();
                    return ra;
                }
            }
            
            List<Term> subterms = term.getSubterms();
            for(int i = 0; i < subterms.size(); i++) {
                path.add(i);
                RuleApplication result = findRA(toplevelSelector, subterms.get(i));
                if(result != null) {
                    return result;
                }
                path.removeLast();
            }

            return null;
        }
    }


    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.auto.strategy.Strategy#findRuleApplication(de.uka.iti.pseudo.proof.ProofNode)
     */
    @Override
    public @Nullable RuleApplication findRuleApplication(ProofNode target)
            throws StrategyException {
        
        Detector detector = new Detector(target);
        
        try {
            return detector.detect();
        } catch (ProofException e) {
            throw new StrategyException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.auto.strategy.AbstractStrategy#init(de.uka.iti.pseudo.proof.Proof, de.uka.iti.pseudo.environment.Environment, de.uka.iti.pseudo.auto.strategy.StrategyManager)
     */
    @Override
    public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {
        
        super.init(proof, env, strategyManager);
        
        this.env = env;
        
        ruleReplaceKnownLeft = env.getRule("replace_known_left");
        ruleReplaceKnownRight = env.getRule("replace_known_right");
        ruleApplyEquality = env.getRule("equality_apply");
        
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Knowledge Strategy";
    }

}
