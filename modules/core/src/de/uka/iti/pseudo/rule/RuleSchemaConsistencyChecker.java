/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.creation.EnvironmentTypingResolver;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.SchemaCollectorVisitor;

/**
 * Check whether all appearances of schema variables have the same type in a rule.
 *
 * This should not fail if the rule is parsed and not created manually.
 *
 * @see EnvironmentTypingResolver
 * @see Rule
 */
class RuleSchemaConsistencyChecker extends SchemaCollectorVisitor {

    /**
     * Checks whether a schema variable always occurs with the same assigned
     * type.
     *
     * This method throws a {@link RuleException} if two equally named schema
     * vars with different types occur on the rule. For instance:
     *
     * <pre>
     * rule ILLEGAL
     *   find %a as int = 42
     *   replace %a as bool
     * </pre>
     *
     * would be rejected.
     *
     * @param rule
     *            the rule to be investigated
     * @throws RuleException
     *             if there is a inconsistently typed schema variable.
     */
    public static void check(@NonNull Rule rule) throws RuleException {
        try {
            RuleSchemaConsistencyChecker checker = new RuleSchemaConsistencyChecker();
            LocatedTerm findClause = rule.getFindClause();
            if(findClause != null) {
                findClause.getTerm().visit(checker);
            }
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
                if (replace != null) {
                    replace.visit(checker);
                }
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

    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        for (SchemaVariable sv : getSchemaVariables()) {
            if(sv.getName().equals(schemaVariable.getName()) &&
                    !sv.getType().equals(schemaVariable.getType())) {
                throw new TermException("Found schema variables with incompatible types:\n" +
                        sv.toString(true) + " and " + schemaVariable.toString(true));
            }
        }
        super.visit(schemaVariable);
    }

}
