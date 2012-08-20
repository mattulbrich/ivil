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
import java.util.List;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Pair;

/**
 * The Class SMTStrategy uses decision procedures to close goals.
 *
 * The closing rule can be configured. Parameters are read from the rule by the decision procedure itself.
 *
 */
public class SMTStrategy extends AbstractStrategy {

    /**
     * The Constant CLOSE_RULE_NAME.
     */
    private static final String CLOSE_RULE_NAME = "auto_smt_close";

    /**
     * The active closing rule.
     */
    private Rule closingRule;

    /**
     * The solver.
     */
    private DecisionProcedure solver;

    /**
     * The env.
     */
    private Environment env;

    private List<Rule> closingRuleCollection;

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.auto.strategy.AbstractStrategy#init(de.uka.iti.pseudo.proof.Proof, de.uka.iti.pseudo.environment.Environment, de.uka.iti.pseudo.auto.strategy.StrategyManager)
     */
    @Override public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {

        super.init(proof, env, strategyManager);
        this.env = env;
        this.closingRuleCollection = findClosingRules(env);

        if(this.closingRuleCollection.isEmpty()) {
            closingRule = null;
            return;
        }

        this.closingRule = closingRuleCollection.get(0);

        // read properties for initial closingRule
        String name = this.getClass().getSimpleName() + ".closingRule";
        if (env.hasProperty(name)) {
            String property = env.getProperty(name);
            Rule rule = env.getRule(property);
            if(rule == null) {
                throw new StrategyException("Closing rule specified in " +
                        "environment does not exist: " + property);
            }
            if(!closingRuleCollection.contains(rule)) {
                throw new StrategyException("Rule specified in environment " +
                        "is not a closing rule: " + property);
            }
            closingRule = rule;
        }

    }

    private static List<Rule> findClosingRules(Environment env) {
        ArrayList<Rule> result = new ArrayList<Rule>();
        for (Rule rule : env.getAllRules()) {
            String decProc = rule.getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
            if(decProc != null) {
                result.add(rule);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.auto.strategy.Strategy#findRuleApplication(de.uka.iti.pseudo.proof.ProofNode)
     */
    @Override
    public RuleApplication findRuleApplication(ProofNode target)
            throws StrategyException, InterruptedException {

        // retire if no rule found
        if(closingRule == null) {
            return null;
        }

        // get the solver if it has not been used yet
        if(solver == null) {
            try {
                String decprocName = closingRule.getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
                solver = env.getPluginManager().getPlugin(DecisionProcedure.SERVICE_NAME,
                        DecisionProcedure.class, decprocName);
            } catch(Exception ex) {
                Log.log(Log.ERROR, "Cannot instantiate decision procedure");
                Log.stacktrace(ex);
                closingRule = null;
                solver = null;
            }
        }

        Sequent sequent = target.getSequent();
        Pair<Result, String> result;

        try {
            result = solver.solve(sequent, env, closingRule.getProperties());
        } catch(InterruptedException e) {
            Log.log(Log.DEBUG, "SMT solver was interrupted, we relay this exception");
            throw e;
        } catch (Exception e) {
            throw new StrategyException(
                 "The SMT solver raised an exception. You may consider changing the strategy.", e);
        }

        boolean proveable = result.fst() == Result.VALID;
        if(proveable) {
            MutableRuleApplication ra = new MutableRuleApplication();
            ra.setProofNode(target);
            ra.setRule(closingRule);
            return ra;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return "SMT Strategy";
    }

    public List<Rule> getClosingRuleCollection() {
        return Collections.unmodifiableList(closingRuleCollection);
    }

    public void setClosingRule(Rule closingRule) {
        this.closingRule = closingRule;
        // invalidate the solver which is cached ...
        this.solver = null;
    }

    public Rule getClosingRule() {
        return closingRule;
    }

}
