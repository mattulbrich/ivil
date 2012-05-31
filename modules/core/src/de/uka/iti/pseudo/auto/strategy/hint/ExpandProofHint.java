/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class RuleProofHint implements a proof hint which applies one rule.
 *
 * Must match with the first entry of succedent
 *
 * @ivildoc "Proof hint/expand"
 *
 * <h2>Proof hint <code>expand</code></h2>
 *
 * In order to expand a recursive definition on a branch, this hint can be used.
 * You can specify how many times a definition is to be unrolled.
 *
 * <h3>Arguments</h3> cut takes one argument which is the name of the rule to be
 * applied.
 * <p>
 * An optional second argument specifies the number of times the definition is
 * to be unrolled.
 *
 * <h3>Example</h3>
 *
 * If there is a rule which defines the recursive function <tt>int f(int)</tt>:
 * <pre>
 * rule f_def
 *   find f(%x)
 *   replace 1+f(%x-1)
 * </pre>
 *
 * A hint <tt>§(expand f_def)</tt> would render a formula <tt>f(2)</tt> to
 * <tt>1+f(2-1)</tt>.
 *
 * <p>A hint <tt>§(expand f_def 2)</tt> would go a step further and expand to
 * <tt>1 + (1 + f(2-1-1))</tt>.
 *
 * <p>(We left the base case unconsidered here.)
 */
public class ExpandProofHint implements ProofHint {

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.environment.Mappable#getKey()
     */
    @Override
    public String getKey() {
        return "expand";
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.auto.strategy.hint.ProofHint#createRuleAppFinder(de.uka.iti.pseudo.environment.Environment, java.util.List)
     */
    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new ExpandHintAppFinder(env, arguments);
    }
}

/**
 * The Class ExpandHintAppFinder implements the rule finder for
 * {@link ExpandProofHint}.
 *
 * A sorted collection keeps the term selectors on which the definition is to be
 * applied. If the all positions have been treated, the depth level is increased
 * and search continued until expansion depth is reached.
 */
class ExpandHintAppFinder extends HintRuleAppFinder {

    /**
     * The definition rule to use to expand.
     */
    private final Rule rule;

    /**
     * The environment to work on.
     */
    private final Environment env;

    /**
     * The expansion depth. The number of recursive expansion to be done.
     */
    private int expandDepth;

    /**
     * The applicable term selectors for the current depth.
     *
     * This is a sorted set, so that the inner terms can be replaced first.
     */
    private final SortedSet<TermSelector> applicableTermSelectors =
            new TreeSet<TermSelector>();

    /**
     * The current depth (which is at most the expansion depth).
     */
    private int currentDepth;

    /**
     * Instantiates a new expand hint app finder.
     *
     * @param env
     *            the environment to work
     * @param arguments
     *            the arguments as parsed by the hint parser
     * @throws StrategyException
     *             if something goes wrong
     */
    public ExpandHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;

        if(arguments.size() != 2 && arguments.size() != 3) {
            throw new StrategyException("The proofhint 'expand' expects one or two argument");
        }

        assert "expand".equals(arguments.get(0));

        String ruleName = arguments.get(1);
        this.rule = env.getRule(ruleName);
        if(rule == null) {
            throw new StrategyException("Unknown rule in proof hint: " + ruleName);
        }

        if(!rule.getAssumptions().isEmpty()) {
            throw new StrategyException("Expand rule must not have assumptions: " + ruleName);
        }

        if(rule.getGoalActions().size() != 1) {
            throw new StrategyException("Expand rule must not split: " + ruleName);
        }

        if(rule.getFindClause().getMatchingLocation() != MatchingLocation.BOTH) {
            throw new StrategyException("Expand rule must match any term: " + ruleName);
        }

        if(arguments.size() == 3) {
            String arg = arguments.get(2);
            try {
                this.expandDepth = Util.parseUnsignedInt(arg);
            } catch (NumberFormatException e) {
                throw new StrategyException("Second parameter is not a natural number: " + arg, e);
            }
        } else {
            this.expandDepth = 1;
        }

        // TODO initialise RuleTreeMatcher once available
    }

    /**
     * {@inheritDoc}
     *
     * <p>Take a term selector from the set of available selectors. If needed increase current
     * depth and find new term selectors.
     *
     * TODO We should think of something if the rule cannot (no longer) be applied.
     *
     */
    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {
        if(currentDepth > expandDepth) {
            return null;
        }

        if(applicableTermSelectors.isEmpty()) {
            currentDepth ++;
            if(currentDepth > expandDepth) {
                return null;
            }
            addTermSelectors(node);
            if(applicableTermSelectors.isEmpty()) {
                return null;
            }
        }

        TermSelector ts = applicableTermSelectors.first();
        applicableTermSelectors.remove(ts);

        RuleApplicationFinder raf = new RuleApplicationFinder(node, env);
        RuleApplicationMaker ram;
        try {
            ram = raf.findOne(ts, rule);
        } catch (ProofException e) {
            throw new StrategyException("Error while instantiating rule " + rule.getName(), e);
        }

        return ram;
    }

    /*
     * add all matches with the find clause on the sequent to
     * applicableTermSelectors.
     */
    private void addTermSelectors(ProofNode node) {
        // TODO use RuleTreeMatch when available.
        Sequent s = node.getSequent();
        int pos = 0;
        for (Term form : s.getAntecedent()) {
            addTermSelectors(new TermSelector(TermSelector.ANTECEDENT, pos), form);
            pos ++;
        }

        pos = 0;
        for (Term form : s.getSuccedent()) {
            addTermSelectors(new TermSelector(TermSelector.SUCCEDENT, pos), form);
            pos ++;
        }
    }

    /*
     * recursively go other a term and try to match against the find clause.
     */
    private void addTermSelectors(TermSelector termSelector, Term form) {
        TermMatcher tm = new TermMatcher();
        Term findClauseFormula = rule.getFindClause().getTerm();
        if(tm.leftMatch(findClauseFormula, form)) {
            applicableTermSelectors.add(termSelector);
        }

        for (int i = 0; i < form.countSubterms(); i++) {
            addTermSelectors(termSelector.selectSubterm(i), form.getSubterm(i));
        }
    }

}