package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class AskDecisionProcedure extends WhereCondition {

    public AskDecisionProcedure() {
        super("askDecisionProcedure");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        
        Sequent sequent = goal.getSequent();
        String decisionProcClass = ruleApp.getRule().getProperty("decisionProcedure");
        
        if(decisionProcClass == null)
            throw new RuleException("The rule does not define a propery 'decisionProcedure' which is must");
        
        try {
            // TODO cache results!
            DecisionProcedure decisionProcedure = 
                (DecisionProcedure) Class.forName(decisionProcClass).newInstance();
            
            Pair<DecisionProcedure.Result, String> res = decisionProcedure.solve(sequent, env, 100);
            
            return res.fst() == DecisionProcedure.Result.VALID; 
            
        } catch (Exception e) {
            throw new RuleException("Error while creating or calling the decision procedure", e);
        }        
    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length > 0)
            throw new RuleException("askDecisionProcedure expects no arguments");        
    }


}
