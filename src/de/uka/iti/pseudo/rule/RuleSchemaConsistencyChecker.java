package de.uka.iti.pseudo.rule;

import de.uka.iti.pseudo.environment.EnvironmentTypingResolver;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.SchemaCollectorVisitor;

/**
 * Check whether all appearences of schema variables have the same type in a rule.
 * 
 * This should not fail if the rule is parsed and not created manually.
 *  
 * @see EnvironmentTypingResolver
 * @see Rule
 */
class RuleSchemaConsistencyChecker extends SchemaCollectorVisitor {

    public static void check(Rule rule) throws RuleException {
        try {
            RuleSchemaConsistencyChecker checker = new RuleSchemaConsistencyChecker();
            rule.getFindClause().getTerm().visit(checker);
            for (LocatedTerm assumption : rule.getAssumptions()) {
                assumption.getTerm().visit(checker);
            }
            for (WhereClause wc : rule.getWhereClauses()) {
                for (Term t : wc.getArguments()) {
                    t.visit(checker);
                }
            }
            for (GoalAction action : rule.getGoalActions()) {
                Term replace = action.getReplaceWith();
                if (replace != null)
                    replace.visit(checker);
                for (Term add : action.getAddAntecedent()) {
                    add.visit(checker);
                }
                for (Term add : action.getAddSuccedent()) {
                    add.visit(checker);
                }
            }
        } catch (TermException e) {
            throw new RuleException("Inconsitency in typing of schema variables", e);
        }
    }
      
    public void visit(SchemaVariable schemaVariable) throws TermException {
        for (SchemaVariable sv : getSchemaVariables()) {
            if(sv.getName().equals(schemaVariable.getName()) && !sv.getType().equals(schemaVariable.getType()))
                throw new TermException("Found schema variables with incompatible types:\n" +
                        sv.toString(true) + " and " + schemaVariable.toString(true));
        }
        super.visit(schemaVariable);
    }

}
