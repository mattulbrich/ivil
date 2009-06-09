package de.uka.iti.pseudo.rule;

import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.PrettyPrint;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Util;

// TODO DOC
public class Rule {
    
    private static final String NEWLINE = "\n";
    
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

    public List<LocatedTerm> getAssumptions() {
        return Util.readOnlyArrayList(assumptions);
    }

    public LocatedTerm getFindClause() {
        return findClause;
    }

    public List<WhereClause> getWhereClauses() {
        return Util.readOnlyArrayList(whereClauses);
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
        // XXX rule checking!!
        // ???
        // e.g.: locations in assumes and finds
        // closegoal is empty
        // newgoal has no replace
        // remove only if find is located 
        // schema variables to always have same type
        RuleSchemaConsistencyChecker.check(this);
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
    
    public String prettyPrint(@NonNull Environment env) {
        StringBuilder sb = new StringBuilder();
        sb.append("rule ").append(getName()).append(NEWLINE);
        sb.append("  find ").append(PrettyPrint.print(env, getFindClause())).append(NEWLINE);
        for (LocatedTerm ass : getAssumptions()) {
            sb.append("  assume ").append(PrettyPrint.print(env, ass)).append(NEWLINE);
        }
        for (WhereClause where : getWhereClauses()) {
            sb.append("  where ").append(where.getWhereCondition().getName());
            for (Term arg : where.getArguments()) {
                sb.append(" ").append(PrettyPrint.print(env, arg));
            }
            sb.append(NEWLINE);
        }
        for (GoalAction action : getGoalActions()) {
            switch(action.getKind()) {
            case CLOSE: sb.append("  closegoal"); break;
            case COPY: sb.append("  samegoal"); break;
            case NEW: sb.append("  newgoal"); break;
            }
            sb.append(NEWLINE);
            Term rep = action.getReplaceWith();
            if(rep != null)
                sb.append("    replace ").append(PrettyPrint.print(env, rep)).append(NEWLINE);
            for (Term t : action.getAddAntecedent()) {
                sb.append("    add ").append(PrettyPrint.print(env, t)).append(" |-").append(NEWLINE);
            }
            for (Term t : action.getAddSuccedent()) {
                sb.append("    add |-").append(PrettyPrint.print(env, t)).append(NEWLINE);
            }
        }
        return sb.toString();
    }
    
    @Override public String toString() {
        return "Rule[" + name + "]";
    }

}
