package de.uka.iti.pseudo.environment;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.where.NotFreeIn;
import de.uka.iti.pseudo.rule.where.Typing;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;

//TODO DOC
public abstract class WhereCondition {
    
    private static Map<String, WhereCondition> whereConditionTable =
        new HashMap<String, WhereCondition>();
    
    private static final WhereCondition CONDITIONS[] =
    {
        new NotFreeIn(),
        new Typing()
    };
    
    static {
        for (WhereCondition wc : CONDITIONS) {
            String name = wc.getName();
            if(whereConditionTable.get(name) != null)
                System.err.println("Warning: where condition " + name + " registered more than once.");
            whereConditionTable.put(name, wc);
        }
    }

    public static WhereCondition getWhereCondition(String identifier) {
        return whereConditionTable.get(identifier);
    }
    
    private String name;
    
    protected WhereCondition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public abstract void tryToApplyTo(Term[] arguments) throws RuleException;

    public abstract boolean applyTo(Term[] arguments, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal) throws RuleException;

}
