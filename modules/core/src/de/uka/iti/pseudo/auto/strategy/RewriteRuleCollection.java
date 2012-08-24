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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationFilter;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.ProgramTerm;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.util.Log;

/**
 * A RewriteRuleCollection allows to find applicable rewrite rules for a goal in
 * a proof.
 *
 * Collection ranges over all rules which have a certain property set to the
 * category of the collection. The category is a string value provided to the
 * constructor.
 *
 * <p>
 * For a rule to be taken into consideration add
 *
 * <pre>
 * tags rewrite &quot;category&quot;
 * </pre>
 *
 * to the end of the rule declaration.
 *
 * Rules are classified according to their toplevel symbol which allows quicker
 * matching since only rules with the appropriate toplevel symbol are checked.
 *
 */
@NonNull
public class RewriteRuleCollection {

    /**
     * The map from toplevel symbols to applicable rules
     */
    private final Map<String, List<Rule>> classificationMap;

    /**
     * A cache of terms which have no applicable rule in this collection. No
     * need to check once again.
     *
     * The mechanism works locally on a term. Hence, rules with assumptions will
     * break the soundness of caching. This can be switched off (set to
     * <code>null</code>) if the category contains assumption-rules.
     */
    private @Nullable Set<Term> noMatchCache =
            Collections.synchronizedSet(new HashSet<Term>());

    /**
     * The environment the rules come from
     */
    private final Environment env;

    /**
     * The category for which we collect rules
     */
    private final String category;

    /**
     * The number of rules in this collection
     */
    private int size;

    /**
     * A filter which filters all found applications.
     */
    private RuleApplicationFilter applicationFilter;

    /**
     * Instantiates a new rewrite rule collection.
     *
     * @param rules
     *            the rules to choose the collection from
     * @param category
     *            the value that the chosen rules need to have for the property
     *            "rewrite"
     * @param env
     *            the environment we work in.
     * @throws StrategyException
     *            if a rule in rules is findless or has assumptions.
     */
    public RewriteRuleCollection(List<Rule> rules, String category,
            Environment env) throws StrategyException {
        this.category = category;
        classificationMap = new HashMap<String, List<Rule>>();
        this.size = 0;
        collectRules(rules, category);
        this.env = env;
    }

    /**
     * Find an applicable rule application in a sequent.
     *
     * We apply a {@link RuleApplicationFinder} first to the antecedent then to
     * the succedent.
     *
     * @param node
     *            the proof node to find a rule for
     *
     * @return a rule application maker if we can find something, null otherwise
     */
    public @Nullable RuleApplicationMaker findRuleApplication(ProofNode node) {

        RuleApplicationFinder finder = new RuleApplicationFinder(node, env);
        finder.setApplicationFilter(applicationFilter);
        Sequent seq = node.getSequent();

        List<Term> ante = seq.getAntecedent();
        RuleApplicationMaker ram = findRuleApplication(finder, ante,
                TermSelector.ANTECEDENT);

        if (ram == null) {
            List<Term> succ = seq.getSuccedent();
            ram = findRuleApplication(finder, succ, TermSelector.SUCCEDENT);
        }

        return ram;

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
     * Select all rules from a list of rules which do belong to a certain
     * category.
     *
     * <p>
     * For a rule to be taken into consideration add
     *
     * <pre>
     * tags rewrite &quot;category&quot;
     * </pre>
     *
     * for the appropriate category to the end of the rule declaration.
     *
     * @param rules
     *            the rules to select a category from
     * @param category
     *            the value that the chosen rules need to have for the property
     *            {@value RuleTagConstants#KEY_REWRITE}
     *
     * @throws RuleException
     *             probably not at all
     */
    private void collectRules(List<Rule> rules, String category)
            throws StrategyException {

        for (Rule rule : rules) {

            String rwProperty = rule.getProperty(RuleTagConstants.KEY_REWRITE);
            if (rwProperty == null || !category.equals(rwProperty)) {
                continue;
            }

            LocatedTerm findClause = rule.getFindClause();

            if (findClause == null) {
                throw new StrategyException("Findless rule " + rule.getName() + " tagged as rewrite.");
            }

            if (noMatchCache != null && rule.getAssumptions().size() != 0) {
                Log.log(Log.WARNING, "Assumption-rule " + rule.getName() +
                        " tagged as rewrite. Switching of caching.");
                deactivateCache();
            }

            String[] classifications = getClassification(findClause.getTerm()).split(",");

            for (String classification : classifications) {
                List<Rule> targetList = classificationMap.get(classification);
                if (targetList == null) {
                    targetList = new LinkedList<Rule>();
                    classificationMap.put(classification, targetList);
                }
                targetList.add(rule);
            }

            size++;
        }

    }

    /**
     * Gets the classification for a term.
     *
     * <p>
     * For most terms, this returns the single classification of the term:
     * <ul>
     * <li>For a binding, get the binder.
     * <li>For an application, get the function symbol.
     * <li>For a program term, get "[program]"
     * <li>For an updated term, get "[updated]"
     * <li>Else: get "[generic]"
     * </ul>
     *
     * <P>
     * For some schematic terms (at the moment {@link SchemaUpdateTerm}s), more
     * than one category can be returned. Categories are then separated by
     * commas. For example the schematic terms <tt>{ U ?}[%a: skip]%b</tt> has
     * the classification "[updated], [program]".
     *
     * @param term
     *            the term to classify
     *
     * @return the classification, comma separated if more than one.
     */
    private @NonNull String getClassification(@NonNull Term term) {

        if (term instanceof Binding) {
            Binding binding = (Binding) term;
            return binding.getBinder().getName();
        }

        if (term instanceof Application) {
            Application app = (Application) term;
            return app.getFunction().getName();
        }

        if (term instanceof SchemaUpdateTerm) {
            SchemaUpdateTerm sut = (SchemaUpdateTerm) term;
            if(sut.isOptional()) {
                return "[updated]," + getClassification(term.getSubterm(0));
            } else {
                return "[updated]";
            }
        }

        if (term instanceof UpdateTerm) {
            return "[updated]";
        }

        if (term instanceof ProgramTerm) {
            // no further classification at the moment
            return "[program]";
        }

        return "[generic]";
    }

    /*
     * for all subterms in terms, select the rules of the classification (and
     * the generic ones) and try to match one against the term. return this
     * match if it exists, null otherwise
     */
    private @Nullable RuleApplicationMaker findRuleApplication(
            RuleApplicationFinder finder, List<Term> terms, boolean side) {
        for (int termno = 0; termno < terms.size(); termno++) {
            Term term = terms.get(termno);
            TermSelector ts = new TermSelector(side, termno); // ok
            RuleApplicationMaker result = findRuleApplication(finder, term, ts);
            if(result != null) {
                return result;
            } else if(noMatchCache != null) {
                // we haven't found a rule application, remember that.
                noMatchCache.add(term);
                if(noMatchCache.size() % 10000 == 0) {
                    System.err.println(category + " : cache size=" + noMatchCache.size());
                }
            }
        }
        return null;
    }

    /**
     * find a rule application for single a term at a position.
     *
     * @param finder the RuleApplicationFinder to be used
     * @param term
     * @param selector
     * @return
     */
    private @Nullable RuleApplicationMaker findRuleApplication(
            RuleApplicationFinder finder, Term term, TermSelector selector) {

        // do not look if the term is known to not match
        if(noMatchCache != null && noMatchCache.contains(term)) {
            return null;
        }

        try {
            List<Rule> ruleset = getRuleSet(term);
            if (ruleset != null && ruleset.size() > 0) {
                RuleApplicationMaker ram = finder.findOne(selector, ruleset);
                if (ram != null) {
                    return ram;
                }
            }

            // try generic ones in the end
            ruleset = classificationMap.get("[generic]");
            if (ruleset != null && ruleset.size() > 0) {
                RuleApplicationMaker ram = finder.findOne(selector,
                        ruleset);
                if (ram != null) {
                    return ram;
                }
            }
        } catch (ProofException e) {
            Log.log(Log.ERROR, "Error while finding rules for " + term);
            Log.log(Log.ERROR, "Continuing anyway");
            Log.stacktrace(e);
        }

        // now go recursively into depth
        for (int i = 0; i < term.countSubterms(); i++) {
            Term t = term.getSubterm(i);
            TermSelector s = selector.selectSubterm(i);
            RuleApplicationMaker ram = findRuleApplication(finder, t, s);
            if (ram != null) {
                return ram;
            }
        }

        return null;
    }

    /**
     * Gets the collection of rules which is applicable to a term (apart from
     * the generic ones)
     * @param term must be a non-schematic term.
     */
    private @Nullable List<Rule> getRuleSet(Term term) {
        String classif = getClassification(term);
        // you should not use this with schematic terms:
        assert !classif.contains(",") : "More than one classification for term: " + term;
        return classificationMap.get(classif);
    }

    @Override
    public String toString() {
        return "RuleCollection[" + category + "] with " + size + " rules";
    }


    /**
     * Deactivate the no-match cache. This should be done if the state
     * applicablility does not only depend on the term but also on non-local
     * factors like assumptions or the result of a filter.
     */
    public void deactivateCache() {
        noMatchCache = null;
    }

    /**
     * clear the no-match cache. This is usually done to cleanup space after
     * ending an automatic run.
     */
    public void clearCache() {
        if(noMatchCache != null) {
            noMatchCache.clear();
        }
    }

    /**
     * get an unmodifiable view of the nomatch cache. Mainly for testing
     * purposes.
     *
     * @return a view to the nomatch-cache, or <code>null</code> if the chache
     *         is deactivated.
     */
    public @Nullable Set<Term> getNoMatchingCache() {
        if(noMatchCache != null) {
            return Collections.unmodifiableSet(noMatchCache);
        } else {
            return null;
        }
    }


}
