package de.uka.iti.pseudo.rule;

import java.util.List;

public class Rule {
    
    private String name;
    private LocatedTerm assumptions[];
    private LocatedTerm findClause;
    private WhereClause whereClauses[];
    private GoalAction goalActions[];

    public Rule(String name, List<LocatedTerm> assumes, LocatedTerm find,
            List<WhereClause> wheres, List<GoalAction> actions) throws RuleException {
        this.name = name;
        this.assumptions = assumes.toArray(new LocatedTerm[assumes.size()]);
        this.findClause = find;
        this.whereClauses = wheres.toArray(new WhereClause[wheres.size()]);
        this.goalActions = wheres.toArray(new GoalAction[actions.size()]);
        
        checkRule();
    }

    private void checkRule() throws RuleException {
    }
    
    public void dump() {

        System.out.println("Rule " + name);
        
        System.out.println("Find:");
        System.out.println(findClause);
        
        System.out.println("Assumptions:");
        for (LocatedTerm lt : assumptions) {
            System.out.println("  " + lt);
        }
        
        System.out.println("Where clauses:");
        for (WhereClause wc : whereClauses) {
            System.out.println("  " + wc);
        }
        
        System.out.println("Actions:");
        for (GoalAction ga : goalActions) {
            ga.dump();
        }
        
        
    }
}
