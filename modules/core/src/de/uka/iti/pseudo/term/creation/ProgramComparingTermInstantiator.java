/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * This class refines the instantiation checking of schema program terms.
 * 
 * Matching statements are not allowed in the super class, here they are, but
 * the schema statement must match the concrete one.
 * 
 * This class is used by {@link ProofNode} to check that a
 * {@link RuleApplication} is indeed allowed.
 * 
 * @see ProofNode#applicable(RuleApplication, Environment)
 * @see ProofNode#apply(RuleApplication, Environment)
 */
public class ProgramComparingTermInstantiator extends TermInstantiator {

    public ProgramComparingTermInstantiator(Map<String, Term> termMap,
            Map<String, Type> typeMap,
            Map<String, Update> updateMap,
            Environment env) {
        super(termMap, typeMap, updateMap);
    }

    @Override
    protected void checkSchemaProgramInstantiation(
            SchemaProgramTerm schema, LiteralProgramTerm prog)
                    throws TermException {

        if (schema.hasMatchingStatement()) {
            Statement schemaStatement = schema.getMatchingStatement();
            Statement statement = prog.getStatement();

            if (statement.getClass() != schemaStatement.getClass()) {
                throw new UnificationException("Incomparable statement types",
                        statement, schemaStatement);
            }

            List<Term> schemaParams = schemaStatement.getSubterms();
            for (int i = 0; i < schemaParams.size(); i++) {
                Term t = schemaParams.get(i);
                t.visit(this);
                Term instantiated = resultingTerm == null ? t : resultingTerm;
                Term original = statement.getSubterms().get(i);
                if (!instantiated.equals(original)) {
                    throw new UnificationException(
                            "Incomparable statement parameter",
                            instantiated, original);
                }
            }
        }

    }

}
