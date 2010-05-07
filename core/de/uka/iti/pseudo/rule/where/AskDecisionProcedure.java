/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import java.util.Map;
import java.util.WeakHashMap;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class AskDecisionProcedure extends WhereCondition {

	private DecisionProcedure decisionProcedure;
    
	private Map<Sequent, DecisionProcedure.Result> cache =
		new WeakHashMap<Sequent, DecisionProcedure.Result>();


    public AskDecisionProcedure() {
        super("askDecisionProcedure");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        
        Sequent sequent = goal.getSequent();
        String decisionProcClass = ruleApp.getRule().getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
        String timeoutString = ruleApp.getRule().getProperty(RuleTagConstants.KEY_TIMEOUT);
        
        if(decisionProcClass == null)
            throw new RuleException("The rule does not define a propery 'decisionProcedure' which is must");
        
        try {
        	if(decisionProcedure == null)
        		decisionProcedure = 
        			(DecisionProcedure) Class.forName(decisionProcClass).newInstance();
            
            long timeout = Long.parseLong(timeoutString);
            
            System.out.println("Solve " + sequent);
            DecisionProcedure.Result res = cache.get(sequent);
            if(res == null) {
            	System.out.println(" ... cache miss");
            	res = decisionProcedure.solve(sequent, env, timeout).fst();
            	cache.put(sequent, res);
            }
            
            return res == DecisionProcedure.Result.VALID; 
            
        } catch (Exception e) {
            throw new RuleException("Error while creating or calling the decision procedure", e);
        }        
    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length > 0)
            throw new RuleException("askDecisionProcedure expects no arguments");        
    }


}
