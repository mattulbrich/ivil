/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.LinkedHashSet;
import java.util.Set;

import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.SchemaProgram;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * The SchemaFinder visitor provides the possibilty to collect all schema
 * variables from a term. This includes schematic program references like [&a].
 * Schema variables of same name but different type are considered different
 * schema variables.
 */
public class SchemaCollectorVisitor extends DefaultTermVisitor.DepthTermVisitor {

    /**
     * The set of collected schema variables, identified by their name
     */
    private Set<SchemaVariable> schemaVariables = new LinkedHashSet<SchemaVariable>();

    /**
     * The set of collected schema modalities.
     */
    private Set<SchemaProgram> schemaPrograms = new LinkedHashSet<SchemaProgram>();

    /**
     * perform collection of schema identifiers in a term
     * 
     * @param t
     *            term to collect
     */
    public void collect(Term t) {
        try {
            t.visit(this);
        } catch (TermException e) {
            // not thrown in this code
            throw new Error();
        }
    }

    /**
     * perform collection of schema identifiers on a rule.
     * This includes:
     * <ul>
     * <li>Find clause
     * <li>Assume clauses
     * <li>Where clauses
     * <li>Replacement clauses
     * <li>Add clauses
     * </ul>
     * 
     * @param rule the rule to inspect
     */
    public void collect(Rule rule) {
        collect(rule.getFindClause().getTerm());
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
            if (replaceWith != null)
                collect(replaceWith);
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
        return schemaPrograms.isEmpty() && schemaVariables.isEmpty();
    }

    /**
     * Gets the set of schema variables.
     * 
     * @return the schema variables
     */
    public Set<SchemaVariable> getSchemaVariables() {
        return schemaVariables;
    }

    /**
     * Gets the set of schema programs.
     * 
     * @return the schema programs
     */
    public Set<SchemaProgram> getSchemaPrograms() {
        return schemaPrograms;
    }

    public void visit(SchemaProgram schemaProgram) throws TermException {
        schemaPrograms.add(schemaProgram);
        Statement statement = schemaProgram.getMatchingStatement();
        if (statement != null) {
            for (Term term : statement.getSubterms()) {
                term.visit(this);
            }
        }
    }
    
    public void visit(UpdateTerm updateTerm) throws TermException {
        super.visit(updateTerm);
        for (AssignmentStatement ass : updateTerm.getAssignments()) {
            ass.getTarget().visit(this);
        }
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        schemaVariables.add(schemaVariable);
    }

}
