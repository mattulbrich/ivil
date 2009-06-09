/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * The SchemaFinder visitor provides the possibilty to collect all schema variables
 * from a term/modality. Schema variables of same name but different type are 
 * considered different schema variables. Therefore schema variables are stored
 * by their name only.
 */
public class SchemaCollectorVisitor extends DefaultTermVisitor.DepthTermVisitor {
    
    /**
     * The set of collected schema variables, identified by their name
     */
    private Set<SchemaVariable> schemaVariables = new HashSet<SchemaVariable>();
    
    /**
     * The set of collected schema modalities.
     */
    private Set<SchemaModality> schemaModalities = new HashSet<SchemaModality>();
    
    /**
     * perform collection of schema identifiers in a term
     * @param t term to collect
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
     * perform collection of schema identifiers in a modality
     * @param m modality to collect
     */
    public void collect(Modality m) {
        try {
            m.visit(this);
        } catch (TermException e) {
            // not thrown in this code
            throw new Error();
        }
    }
    
    public void collect(Rule rule) {
        // TODO method documentation
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
            if(replaceWith != null)
                collect(replaceWith);
            for (Term term : goalAction.getAddAntecedent()) {
                collect(term);
            }
            for (Term term : goalAction.getAddSuccedent()) {
                collect(term);
            }
        }
    }

    public void visit(SchemaModality schemaModality) throws TermException {
        schemaModalities.add(schemaModality);
    }
    
    public void visit(SchemaVariable schemaVariable) throws TermException {
        schemaVariables.add(schemaVariable);
    }
    
    /**
     * Checks if is empty, i.e. neither schema variables nor modalities have
     * been found.
     * 
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return schemaModalities.isEmpty() && schemaVariables.isEmpty();
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
     * Gets the set of schema modalities.
     * 
     * @return the schema modalities
     */
    public Set<SchemaModality> getSchemaModalities() {
        return schemaModalities;
    }

}
