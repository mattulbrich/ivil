package de.uka.iti.pseudo.rule;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.util.Util;

public class Rule {
    
    private String name;
    private LocatedTerm assumptions[];
    private LocatedTerm findClause;
    private WhereClause whereClauses[];
    private GoalAction goalActions[];
    private Map<String, String> properties;
    
    public String getProperty(String string) {
        return properties.get(string);
    }

    public String getName() {
        return name;
    }

    public LocatedTerm[] getAssumptions() {
        return assumptions;
    }

    public LocatedTerm getFindClause() {
        return findClause;
    }

    public WhereClause[] getWhereClauses() {
        return whereClauses;
    }

    public GoalAction[] getGoalActions() {
        return goalActions;
    }

    public Rule(String name, List<LocatedTerm> assumes, LocatedTerm find,
            List<WhereClause> wheres, List<GoalAction> actions,
            Map<String, String> properties)
            throws RuleException {
        this.name = name;
        this.assumptions = Util.listToArray(assumes, LocatedTerm.class);
        this.findClause = find;
        this.whereClauses = Util.listToArray(wheres, WhereClause.class);
        this.goalActions = Util.listToArray(actions, GoalAction.class);
        this.properties = properties;
        checkRule();
    }

    private void checkRule() throws RuleException {
        // TODO
        // e.g.: locations in assumes and finds
        // closegoal is empty
        // newgoal has no replace
    }
    
    public void dump() {

        System.out.println("  Rule " + name);
        
        System.out.print("    Find: ");
        System.out.println(findClause);
        
        System.out.println("    Assumptions:");
        for (LocatedTerm lt : assumptions) {
            System.out.println("      " + lt);
        }
        
        System.out.println("    Where clauses:");
        for (WhereClause wc : whereClauses) {
            System.out.println("      " + wc);
        }
        
        System.out.println("    Actions:");
        for (GoalAction ga : goalActions) {
            ga.dump();
        }
        
        
    }

}
