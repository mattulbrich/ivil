package de.uka.iti.pseudo.rule.where;

import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO Documentation needed
public abstract class SimpleWhereCondition extends WhereCondition {

    protected SimpleWhereCondition(String name) {
        super(name);
    }

    @Override 
    public final boolean applyTo(Term[] arguments,
            TermUnification mc, 
            RuleApplication ruleApp, 
            ProofNode goal,
            Environment env, 
            Map<String, String> properties, 
            boolean commit)  throws RuleException {
        
        return applyTo(arguments, mc);
    }
    
    /**
     * Apply to.
     * 
     * @param whereClause the where clause
     * @param mc the mc
     * 
     * @return true, if successful
     * 
     * @throws RuleException the rule exception
     */
    protected abstract boolean applyTo(Term[] arguments, TermUnification mc) throws RuleException;

}
