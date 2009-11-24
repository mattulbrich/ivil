package de.uka.iti.pseudo.auto.strategy;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Pair;

public class SMTStrategy extends AbstractStrategy {
    
    private static final String CLOSE_RULE_NAME = "auto_smt_close";
    private Rule closeRule;
    private DecisionProcedure solver;
    private long timeout;
    private Environment env;

    @Override public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {

        super.init(proof, env, strategyManager);
        this.env = env;
        this.closeRule = env.getRule(CLOSE_RULE_NAME);
        try {
            String className = closeRule.getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
            solver = (DecisionProcedure) Class.forName(className).newInstance();
            timeout = Long.parseLong(closeRule.getProperty(RuleTagConstants.KEY_TIMEOUT));
        } catch(Exception ex) {
            System.err.println("Cannot instantiate background decision procedure");
            ex.printStackTrace();
            closeRule = null;
        }
    }

    @Override protected RuleApplication findRuleApplication(int goalIndex)
            throws StrategyException {
        if(solver == null)
            return null;

        Sequent sequent = getProof().getGoal(goalIndex).getSequent();
        Pair<Result, String> result;
        
        try {
            result = solver.solve(sequent, env, timeout);
        } catch (Exception e) {
            throw new StrategyException("The SMT solver raised an exception. You may consider changing the strategy.");
        }
        
        boolean proveable = result.fst() == Result.VALID;
        if(proveable) {
            MutableRuleApplication ra = new MutableRuleApplication();
            ra.setGoalNumber(goalIndex);
            ra.setRule(closeRule);
            return ra;
        }
        
        return null;
    }
    
    @Override public String toString() {
        return "SMT Strategy";
    }

}
