/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.LinkedHashSet;
import java.util.Set;

import nonnull.NonNull;

import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Log;

/**
 * The SchemaFinder visitor provides the possibility to collect all schema
 * variables from a term. Schema variables of same name but different type are
 * considered different schema variables.
 */
public class SchemaCollectorVisitor extends DefaultTermVisitor.DepthTermVisitor {

    /**
     * The set of collected schema variables, identified by their name.
     */
    private final Set<SchemaVariable> schemaVariables = new LinkedHashSet<SchemaVariable>();

    /**
     * Perform collection of schema identifiers in a term.
     *
     * @param t
     *            term in which they are to be collect
     */
    public void collect(@NonNull Term t) {
        try {
            t.visit(this);
        } catch (TermException e) {
            // not thrown in this code
            Log.stacktrace(Log.ERROR, e);
            throw new Error(e);
        }
    }

    /**
     * Perform collection of schema identifiers on a statement.
     *
     * This applies the search on all arguments to the statement.
     *
     * @param statement
     *            statement to collect
     */
    public void collect(@NonNull Statement statement) {
        try {
            for (Term subterm : statement.getSubterms()) {
                subterm.visit(this);
            }
        } catch (TermException e) {
            // not thrown in this code
            Log.stacktrace(Log.ERROR, e);
            throw new Error();
        }
    }

    /**
     * Perform collection of schema identifiers on a rule. This includes:
     * <ul>
     * <li>Find clause
     * <li>Assume clauses
     * <li>Where clauses
     * <li>Replacement clauses
     * <li>Add clauses
     * </ul>
     *
     * @param rule
     *            the rule to inspect
     */
    public void collect(Rule rule) {
        LocatedTerm findClause = rule.getFindClause();
        if (findClause != null) {
            collect(findClause.getTerm());
        }
        for (LocatedTerm lterm : rule.getAssumptions()) {
            collect(lterm.getTerm());
        }
        for (WhereClause whereClause : rule.getWhereClauses()) {
            for (Term term : whereClause.getArguments()) {
                collect(term);
            }
        }
        for (GoalAction goalAction : rule.getGoalActions()) {
            Term replaceWith = goalAction.getReplaceWith();
            if (replaceWith != null) {
                collect(replaceWith);
            }
            for (Term term : goalAction.getAddAntecedent()) {
                collect(term);
            }
            for (Term term : goalAction.getAddSuccedent()) {
                collect(term);
            }
        }
    }

    /**
     * Checks if is empty, i.e. neither schema variables nor modalities have
     * been found.
     *
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return schemaVariables.isEmpty();
    }

    /**
     * Gets the collected set of schema variables.
     *
     * @return the schema variables
     */
    public Set<SchemaVariable> getSchemaVariables() {
        return schemaVariables;
    }

    @Override
    public void visit(SchemaProgramTerm schemaProgram) throws TermException {
        super.visit(schemaProgram);
        Statement statement = schemaProgram.getMatchingStatement();
        if (statement != null) {
            for (Term term : statement.getSubterms()) {
                term.visit(this);
            }
        }
    }

    @Override
    public void visit(UpdateTerm updateTerm) throws TermException {
        super.visit(updateTerm);
        for (Assignment ass : updateTerm.getAssignments()) {
            ass.getTarget().visit(this);
        }
    }

    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        schemaVariables.add(schemaVariable);
    }

}
