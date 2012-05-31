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

public class SMTStrategy extends AbstractStrategy {

    private static final String CLOSE_RULE_NAME = "auto_smt_close";
    private Rule closeRule;
    private DecisionProcedure solver;
    private Environment env;

    @Override public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {

        super.init(proof, env, strategyManager);
        this.env = env;
        this.closeRule = env.getRule(CLOSE_RULE_NAME);

        // bug BOOKEY-25
        // decision procedure rule was not defined
        if(closeRule == null) {
            return;
        }

        try {
            String decprocName = closeRule.getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
            solver = env.getPluginManager().getPlugin(DecisionProcedure.SERVICE_NAME,
                    DecisionProcedure.class, decprocName);
        } catch(Exception ex) {
            Log.log(Log.ERROR, "Cannot instantiate background decision procedure");
            ex.printStackTrace();
            closeRule = null;
        }
    }

    @Override
    public RuleApplication findRuleApplication(ProofNode target)
            throws StrategyException {

        // retire if no solver found
        if(solver == null) {
            return null;
        }

        Sequent sequent = target.getSequent();
        Pair<Result, String> result;

        try {
            result = solver.solve(sequent, env, closeRule.getProperties());
        } catch (Exception e) {
            throw new StrategyException("The SMT solver raised an exception. You may consider changing the strategy.");
        }

        boolean proveable = result.fst() == Result.VALID;
        if(proveable) {
            MutableRuleApplication ra = new MutableRuleApplication();
            ra.setProofNode(target);
            ra.setRule(closeRule);
            return ra;
        }

        return null;
    }

    @Override public String toString() {
        return "SMT Strategy";
    }

}
