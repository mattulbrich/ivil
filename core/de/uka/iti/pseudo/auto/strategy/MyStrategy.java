/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFilter;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

/**
 * The Class SimplificationStrategy.
 */
public class MyStrategy implements Strategy, RuleApplicationFilter {
    
    /**
     * SplitMode lists all possibilities to handle splitting:
     * <ol>
     * <li>Do it whenever a splitting rule application is found
     * <li>Never do it
     * <li>Do it only if no program terms are anywhere on the sequent 
     * (symbolic execution first)
     * </ol>
     */
    public enum SplitMode {
        SPLIT, DONT_SPLIT, SPLIT_NO_PROGRAMS
    };
    
    /**
     * Collects all categories taken into account in this strategy.
     */
    private final static String[] REWRITE_CATEGORIES = {
        "close",
        "concrete",
        "updSimpl",
        "prop simp",
        "fol simp",
    };
    
    /**
     * The category name of splitting rules. This is kept separate from the
     * others to allow to install a filter on the collection.
     */
    private final static String SPLIT_CATEGORY = "split";
    
    /**
     * Use this {@link TermVisitor} to detect programs in an term.
     * @see #accepts(RuleApplication)
     */
    private static TermVisitor PROGRAM_DETECTOR = new DefaultTermVisitor.DepthTermVisitor() {
        public void visit(LiteralProgramTerm programTerm) throws TermException {
            throw new TermException("Program found!");
        }
    };
    
    /**
     * The proof upon which we work. set in
     * {@link #init(Proof, Environment, StrategyManager)}, never changed
     * afterwards.
     */
    private Proof proof;
    
    /**
     * The currently active split mode.
     */
    private SplitMode splitMode = SplitMode.SPLIT_NO_PROGRAMS;
    
    /**
     * The rewrite rule handlers for the various categories. Length coincides
     * with the length of {@link #REWRITE_CATEGORIES} plus 1 (for split)
     */
    private RewriteRuleCollection ruleCollections[];
    
    /**
     * Try all {@link RewriteRuleCollection}s to find a rule application.
     * Return the first found rule application.
     * 
     * @return the first found rule application, null if none found.
     */
    public RuleApplication findRuleApplication() {
        
        assert proof != null;
        
        List<ProofNode> openGoals = proof.getOpenGoals();
        for (int i = 0; i < openGoals.size(); i++) {
            RuleApplicationMaker ram = findRuleApplication(i);
            if(ram != null) {
                ram.setGoalNumber(i);
                return ram;
            }
        }
        
        return null;
    }

    /*
     * Find rule application on a certain goal. Try all collections.
     */
    private RuleApplicationMaker findRuleApplication(int goalNo) {
        
        assert ruleCollections != null;
        
        for (int i = 0; i < ruleCollections.length; i++) {
            RuleApplicationMaker ruleApplication = ruleCollections[i].findRuleApplication(proof, goalNo);
            if(ruleApplication != null)
                return ruleApplication;
        }

        return null;
    }

    @Override 
    public void init(@NonNull Proof proof, @NonNull Environment env, @NonNull StrategyManager strategyManager)
            throws StrategyException {
        
        this.proof = proof;
        
        ruleCollections = new RewriteRuleCollection[REWRITE_CATEGORIES.length + 1];
        List<Rule> allRules = env.getAllRules();
        for (int i = 0; i < ruleCollections.length; i++) {
            try {
                ruleCollections[i] = new RewriteRuleCollection(allRules, REWRITE_CATEGORIES[i], env);
            } catch (RuleException e) {
                throw new StrategyException("Cannot initialise MyStrategy", e);
            }
        }
        
        // create the splitting rule collection which uses "this" as filter.
        try {
            ruleCollections[REWRITE_CATEGORIES.length] = new RewriteRuleCollection(allRules, SPLIT_CATEGORY, env);
            ruleCollections[REWRITE_CATEGORIES.length].setApplicationFilter(this);
        } catch (RuleException e) {
            throw new StrategyException("Cannot initialise MyStrategy", e);
        }
    }
    
    
    /**
     * Filter acceptable rule applications. This is only relevant for splitting
     * rules. Check the sequent for program terms if set so ...
     * 
     * @return <code>true</code> if current mode is {@link SplitMode#SPLIT} or
     *         no programs on the sequent to be treated and current mode is
     *         {@link SplitMode#SPLIT_NO_PROGRAMS}, otherwise
     *         <code>false</code>
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
                Sequent seq = proof.getGoal(ruleApp.getGoalNumber())
                        .getSequent();
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
    
    
    //
    // getter and setter
    //

    @Override
    public String toString() {
        return "Test Strategy";
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
