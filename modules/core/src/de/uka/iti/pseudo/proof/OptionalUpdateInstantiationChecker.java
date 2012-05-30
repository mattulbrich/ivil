/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor.DepthTermVisitor;

/**
 * The visitor OptionalUpdateInstantiationChecker is used to check that schema
 * update identifiers instantiated with empty updates appear only optional in
 * find clauses and assumptions.
 */
class OptionalUpdateInstantiationChecker extends DepthTermVisitor {

    private Set<String> nonOptionalSchemaUpdates = new HashSet<String>();

    /**
     * Check that schema update identifiers instantiated with empty updates
     * appear only optional in find clauses and assumptions.
     * 
     * @param ruleApp
     *            the rule application to take clauses and instantiations from
     * @throws ProofException
     *             if a schema updated is instantiated with
     *             {@link Update#EMPTY_UPDATE} but appears non-optional in a
     *             find or assume clause.
     */
    public static void check(RuleApplication ruleApp) throws ProofException {
        OptionalUpdateInstantiationChecker checker = null;
        for (Map.Entry<String, Update> entry : ruleApp.getSchemaUpdateMapping().entrySet()) {
            if(!entry.getValue().isEmpty()) {
                continue;
            }

            // otherwise ensure that the schema update appears only optional in
            // find and assume clause
            if(checker == null) {
                checker = new OptionalUpdateInstantiationChecker();
                Rule rule = ruleApp.getRule();
                try {
                    rule.getFindClause().getTerm().visit(checker);
                    for (LocatedTerm assume : rule.getAssumptions()) {
                        assume.getTerm().visit(checker);
                    }
                } catch (TermException e) {
                    // does not appear in code
                    throw new Error(e);
                }
            }
            
            if(checker.nonOptionalSchemaUpdates.contains(entry.getKey())) {
                throw new ProofException("Schema update identifier "
                        + entry.getKey() + " appears non-optional in rule "
                        + ruleApp.getRule().getName()
                        + " but is instantiated with empty update");
            }
        }
    }
    
    @Override
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        if(!schemaUpdateTerm.isOptional()) {
            nonOptionalSchemaUpdates.add(schemaUpdateTerm.getSchemaIdentifier());
        }
        super.visit(schemaUpdateTerm);
    }

}
