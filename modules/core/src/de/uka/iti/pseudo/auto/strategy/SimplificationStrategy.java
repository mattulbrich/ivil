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

import java.util.List;

import checkers.nullness.quals.LazyNonNull;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFilter;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

/**
 * The Class SimplificationStrategy.
 */
public class SimplificationStrategy extends AbstractStrategy implements
        RuleApplicationFilter {

    /**
     * SplitMode lists all possibilities to handle splitting:
     * <ol>
     * <li>Do it whenever a splitting rule application is found
     * <li>Never do it
     * <li>Do it only if no program terms are anywhere on the sequent (symbolic
     * execution first)
     * </ol>
     */
    public static enum SplitMode {
        SPLIT, DONT_SPLIT, SPLIT_NO_PROGRAMS
    };

    /**
     * Collects all categories taken into account in this strategy.
     */
    private final static String[] REWRITE_CATEGORIES = { "updSimpl", "close",
            "concrete", "prop simp", "fol simp", "fol add" };

    /**
     * The category name of splitting rules. This is kept separate from the
     * others to allow to install a filter on the collection.
     */
    private final static String SPLIT_CATEGORY = "split";

    /**
     * Use this {@link TermVisitor} to detect programs in an term.
     * 
     * @see #accepts(RuleApplication)
     */
    private static TermVisitor PROGRAM_DETECTOR = new DefaultTermVisitor.DepthTermVisitor() {
        @Override
        public void visit(LiteralProgramTerm programTerm) throws TermException {
            throw new TermException("Program found!");
        }
    };

//    /**
//     * store for all categories those proof node which did not match and do not
//     * try to match again.
//     */
//    private Set<ProofNode> /*@LazyNonNull*/[] noMatchNodes = null;

    /**
     * The currently active split mode.
     */
    private SplitMode splitMode = SplitMode.SPLIT_NO_PROGRAMS;

    /**
     * The rewrite rule handlers for the various categories. Length coincides
     * with the length of {@link #REWRITE_CATEGORIES} plus 1 (for split)
     */
    private RewriteRuleCollection ruleCollections /*@LazyNonNull*/[] = null;

    /*
     * Find rule application on a certain goal. Try all collections.
     */
    @Override
    public @Nullable RuleApplicationMaker findRuleApplication(@NonNull ProofNode target) {

        assert ruleCollections != null;

        for (int collNo = 0; collNo < ruleCollections.length; collNo++) {

            RuleApplicationMaker ruleApplication = ruleCollections[collNo]
                    .findRuleApplication(target);
            if (ruleApplication != null) {
                ruleApplication.setProofNode(target);
                return ruleApplication;
            }

        }

        return null;
    }

    @Override
    public void init(@NonNull Proof proof, @NonNull Environment env,
            @NonNull StrategyManager strategyManager) throws StrategyException {
        super.init(proof, env, strategyManager);

        ruleCollections = new RewriteRuleCollection[REWRITE_CATEGORIES.length + 1];
        List<Rule> allRules = env.getAllRules();
        for (int i = 0; i < REWRITE_CATEGORIES.length; i++) {
            ruleCollections[i] = new RewriteRuleCollection(allRules,
                    REWRITE_CATEGORIES[i], env);
        }

        // create the splitting rule collection which uses "this" as filter.
        ruleCollections[REWRITE_CATEGORIES.length] = new RewriteRuleCollection(
                allRules, SPLIT_CATEGORY, env);
        ruleCollections[REWRITE_CATEGORIES.length]
                .setApplicationFilter(this);

        // check if env asks us to change split mode
        {
            String value = env.getProperty(this.getClass().getSimpleName()
                    + ".splitMode");
            try {
                if (null != value)
                    setSplitMode(SplitMode.valueOf(value));
            } catch (IllegalArgumentException e) {
                throw new StrategyException(
                        "The problem environment specified a value \""
                                + value
                                + "\", which does not name a valid Mode for SimplificationStrategy.splitMode",
                        e);
            }
        }
    }

    /**
     * Filter acceptable rule applications. This is only relevant for splitting
     * rules. Check the sequent for program terms if set so ...
     * 
     * @return <code>true</code> if current mode is {@link SplitMode#SPLIT} or
     *         no programs on the sequent to be treated and current mode is
     *         {@link SplitMode#SPLIT_NO_PROGRAMS}, otherwise <code>false</code>
     */

    @Override
    public boolean accepts(RuleApplication ruleApp) {
        switch (splitMode) {
        case SPLIT:
            // allow always
            return true;

        case DONT_SPLIT:
            // allow never
            return false;

        case SPLIT_NO_PROGRAMS:
            // allow only if no programs on the sequent
            try {
                Sequent seq = ruleApp.getProofNode().getSequent();
                for (Term t : seq.getAntecedent()) {
                    t.visit(PROGRAM_DETECTOR);
                }
                for (Term t : seq.getSuccedent()) {
                    t.visit(PROGRAM_DETECTOR);
                }
                // no exception raised: no program terms
                return true;
            } catch (TermException e) {
                // exception raised: program found
                return false;
            }
        }
        throw new Error("Unreachable");
    }

    /*
     * invoked after an automated proof finishes: forget everything about
     * possible unmatching proofnodes: frees memory.
     */
    @Override
    public void endSearch() {
        for (RewriteRuleCollection collection : ruleCollections) {
            collection.clearCache();
        }
    }

    //
    // getter and setter
    //

    @Override
    public String toString() {
        return "Simplification";
    }

    /**
     * Gets the split mode.
     * 
     * @return the split mode
     */
    public SplitMode getSplitMode() {
        return splitMode;
    }

    /**
     * Sets the split mode.
     * 
     * @param splitMode
     *            the new split mode
     */
    public void setSplitMode(SplitMode splitMode) {
        this.splitMode = splitMode;
    }

}
