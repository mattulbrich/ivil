package de.uka.iti.pseudo.auto.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nonnull.NonNull;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.ProgramTerm;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.creation.TermMatcher;

public final class RuleMatchTreeCollection {

    /**
     * The map from toplevel symbols to applicable rules
     */
    private final Map<String, List<Rule>> classificationMap;

    /**
     * A cache of terms to theoretically applicable rules. "Theoretically"
     * applicable means: The find clauses matches. Where conditions or
     * assumptions do not need to hold.
     *
     * This is not limited to toplevel terms hence.
     *
     */
    private final Map<Term, RuleMatchTree> ruleMatchCache =
            new ConcurrentHashMap<Term, RuleMatchTree>();

    /**
     * The number of rules in this collection
     */
    private int size;

    public RuleMatchTreeCollection(List<Rule> rules) throws StrategyException {
        this.classificationMap = new HashMap<String, List<Rule>>();
        collectRules(rules);
    }

    private void collectRules(List<Rule> rules) throws StrategyException {
        for (Rule rule : rules) {

            LocatedTerm findClause = rule.getFindClause();

            if (findClause == null) {
                throw new StrategyException("RuleMatchTrees cannot work on findless rules: " + rule);
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
    private static @NonNull String getClassification(@NonNull Term term) {

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

    public RuleMatchTree getRuleMatchTree(Term term) {
        RuleMatchTree result = ruleMatchCache.get(term);
        if(result != null) {
            return result;
        }

        result = calcRuleMatchTree(term);

        ruleMatchCache.put(term, result);
        return result;
    }

    private RuleMatchTree calcRuleMatchTree(Term term) {
        String classif = getClassification(term);
        List<Rule> rules = getRulesForClassification(classif);
        List<Rule> matchingRules = new ArrayList<Rule>();
        for (Rule rule : rules) {
            LocatedTerm findClause = rule.getFindClause();
            assert findClause != null : "findless must have been excluded earlier!";
            TermMatcher termMatcher = new TermMatcher();
            boolean canMatch = termMatcher.leftMatch(findClause.getTerm(), term);
            if(canMatch) {
                matchingRules.add(rule);
            }
        }

        List<RuleMatchTree> subTrees = new ArrayList<RuleMatchTree>();
        for (Term subterm : term.getSubterms()) {
            subTrees.add(getRuleMatchTree(subterm));
        }

        return new RuleMatchTree(term, matchingRules, subTrees);
    }


    private List<Rule> getRulesForClassification(String classif) {
        List<Rule> rules = classificationMap.get(classif);
        if(rules == null) {
            return Collections.emptyList();
        } else {
            return rules;
        }
    }


    public void clearCache() {
        ruleMatchCache.clear();
    }

}
