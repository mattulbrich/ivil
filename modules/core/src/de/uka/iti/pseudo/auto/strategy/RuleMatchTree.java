package de.uka.iti.pseudo.auto.strategy;

import java.util.Collections;
import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public final class RuleMatchTree {

    private final Term term;
    private final List<Rule> matchingRules;
    private final List<RuleMatchTree> branches;

    /**
     * @param term
     * @param matchingRules
     * @param branches
     */
    public RuleMatchTree(@NonNull Term term,
            @DeepNonNull List<Rule> matchingRules,
            @DeepNonNull List<RuleMatchTree> branches) {

        assert term.countSubterms() == branches.size();

        this.term = term;
        this.matchingRules = matchingRules;
        this.branches = branches;
    }

    public void deepCollectMatchingRules(TermSelector termSelector,
            List<Pair<TermSelector, Rule>> result) {
        for (Rule match : matchingRules) {
            result.add(Pair.make(termSelector, match));
        }

        int pos = 0;
        for (RuleMatchTree branch : branches) {
            branch.deepCollectMatchingRules(termSelector.selectSubterm(pos), result);
            pos ++;
        }
    }

    /**
     * @return the term
     */
    public Term getTerm() {
        return term;
    }

    /**
     * @return the matchingRules
     */
    public List<Rule> getMatchingRules() {
        return Collections.unmodifiableList(matchingRules);
    }

    /**
     * @return the branches
     */
    public List<RuleMatchTree> getBranches() {
        return Collections.unmodifiableList(branches);
    }

}
