/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

/**
 * The Class SimplificationStrategy2 should eventually replace
 * {@link SimplificationStrategy}.
 */
public final class FastSimplificationStrategy extends AbstractStrategy {

    /** TODO ivildoc this! */
    private static final String REWRITE_LEVELS_PROPERTY = "rewrite.categories";

    private RuleMatchTreeCollection ruleCollection;

    private final Comparator<Pair<TermSelector, Rule>> matchSorter =
            new Comparator<Pair<TermSelector, Rule>>() {
        @Override
        public int compare(Pair<TermSelector, Rule> p1,
                Pair<TermSelector, Rule> p2) {
            int level1 = ruleLevelMap.get(p1.snd());
            int level2 = ruleLevelMap.get(p2.snd());
            return level1 - level2;
        }
    };

    private final Map<Rule, Integer> ruleLevelMap = new HashMap<Rule, Integer>();
    private Environment env;

    @Override
    public @Nullable RuleApplicationMaker findRuleApplication(@NonNull ProofNode target) throws StrategyException {

        List<Pair<TermSelector, Rule>> matchingRules = findAllMatches(target.getSequent());
        Collections.sort(matchingRules, getMatchSorter());

        RuleApplicationFinder ruleAppFinder = new RuleApplicationFinder(target, env);

        for (Pair<TermSelector, Rule> pair : matchingRules) {
            try {
                RuleApplicationMaker ram =
                        ruleAppFinder.findOne(pair.fst(), pair.snd());
                if(ram != null) {
                    return ram;
                }
            } catch (ProofException e) {
                throw new StrategyException("Strategy failed due to a proof error", e);
            }
        }

        return null;
    }

    // package visible for testing
    List<Pair<TermSelector, Rule>> findAllMatches(Sequent sequent) {
        List<Pair<TermSelector, Rule>> result = new ArrayList<Pair<TermSelector,Rule>>();

        int pos = 0;
        for (Term t : sequent.getAntecedent()) {
            ruleCollection.getRuleMatchTree(t).deepCollectMatchingRules(
                    new TermSelector(TermSelector.ANTECEDENT, pos), result);
            pos ++;
        }

        pos = 0;
        for (Term t : sequent.getSuccedent()) {
            ruleCollection.getRuleMatchTree(t).deepCollectMatchingRules(
                    new TermSelector(TermSelector.SUCCEDENT, pos), result);
            pos ++;
        }

        return result;
    }

    @Override
    public void init(@NonNull Proof proof, @NonNull Environment env,
            @NonNull StrategyManager strategyManager) throws StrategyException {
        super.init(proof, env, strategyManager);

        this.env = env;

        List<Rule> rules = filterRewriteRules(env.getAllRules());
        ruleCollection = new RuleMatchTreeCollection(rules);
        Map<String, String> symbolicLevels = makeSymbolicLevels();
        for (Rule rule : rules) {
            ruleLevelMap.put(rule, getLevel(rule, symbolicLevels));
        }
    }

    private List<Rule> filterRewriteRules(List<Rule> allRules) {
        ArrayList<Rule> result = new ArrayList<Rule>();
        for (Rule rule : allRules) {
            if(rule.getProperty(RuleTagConstants.KEY_REWRITE) != null) {
                result.add(rule);
            }
        }
        return result;
    }

    private Map<String, String> makeSymbolicLevels() {
        String property = env.getProperty(REWRITE_LEVELS_PROPERTY);

        if(property == null) {
            return Collections.emptyMap();
        }

        Map<String,String> result = new HashMap<String, String>();
        String[] defs = property.split(",");
        for (String def : defs) {
            String[] pair = def.split("=");
            if(pair.length == 2) {
                result.put(pair[0].trim(), pair[1].trim());
            }
        }

        return result;
    }

    private int getLevel(Rule rule, Map<String, String> symbolicLevels) throws StrategyException {
        String property = rule.getProperty(RuleTagConstants.KEY_REWRITE);
        String lookup = symbolicLevels.get(property);

        if(lookup == null) {
            lookup = property;
        }

        int result;
        try {
            result = Integer.parseInt(lookup);
        } catch(NumberFormatException ex) {
            throw new StrategyException(
                    "Unknown rewrite level (numeric or symbolic) " + property +
                    " in rule " + rule.getName());
        }

        if(result < 0) {
            throw new StrategyException("Negative rewrite level in rule " + rule.getName());
        }

        return result;
    }

    @Override
    public void endSearch() {
        ruleCollection.clearCache();
    }

    //
    // getter and setter
    //

    @Override
    public String toString() {
        return "FastSimplification";
    }

    public Comparator<Pair<TermSelector, Rule>> getMatchSorter() {
        return matchSorter;
    }

    /**
     * @return the ruleLevelMap
     */
    public Map<Rule, Integer> getRuleLevelMap() {
        return Collections.unmodifiableMap(ruleLevelMap);
    }

}
