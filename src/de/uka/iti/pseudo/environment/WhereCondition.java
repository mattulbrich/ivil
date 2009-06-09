package de.uka.iti.pseudo.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.rule.where.NewSkolem;
import de.uka.iti.pseudo.rule.where.NotFreeIn;
import de.uka.iti.pseudo.rule.where.Subst;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;

//TODO DOC
public abstract class WhereCondition {
    
    private static Map<String, WhereCondition> whereConditionTable =
        new HashMap<String, WhereCondition>();
    
    private static final WhereCondition CONDITIONS[] =
    {
        new NotFreeIn(),
        // new Typing(),
        new NewSkolem(),
        new Subst()
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

    public boolean applyTo(WhereClause whereClause, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal, Environment env, Properties properties) throws RuleException {
        return applyTo(whereClause, mc);
    }
    
    protected abstract boolean applyTo(WhereClause whereClause, TermUnification mc) throws RuleException;
    
    public void wasImported(WhereClause whereClause, Environment env, Properties properties) throws RuleException {
        // default behaviour is to do nothing.
    }

}
