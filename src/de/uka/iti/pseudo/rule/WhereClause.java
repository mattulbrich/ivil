package de.uka.iti.pseudo.rule;

import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.MatchingContext;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Term;

public class WhereClause {
    
    private WhereCondition whereCondition;
    private Term[] arguments;

    public WhereClause(WhereCondition where, Term[] terms) throws RuleException {
        this.arguments = terms;
        this.whereCondition = where;
        
        where.tryToApplyTo(arguments);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(whereCondition.getName());
        for (Term arg : arguments) {
            sb.append(" " + arg);
        }
        return sb.toString();
    }

    public boolean applyTo(MatchingContext mc, RuleApplication ruleApp,
            ProofNode goal) {
        return whereCondition.applyTo(arguments, mc, ruleApp, goal);
    }
    
}
