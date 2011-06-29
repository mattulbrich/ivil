/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;
import checkers.nullness.quals.AssertNonNullIfTrue;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class SchemaProgramTerm is used to model a program term with a statement
 * to be matched against a literal program term.
 * 
 * It contains a schema variable which contains the whole term after matching
 * and a matching statement which is matched against the referenced statement of
 * a literal program term. If the matching statement is null, no matching is
 * performed.
 */
public final class SchemaProgramTerm extends ProgramTerm {

    /**
     * The matching statement to match against.
     */
    private @Nullable Statement matchingStatement;

    /**
     * Instantiates a new schema program term.
     * 
     * <p>
     * The constructor is not visible. Use the {@code getInst} methods to
     * get/create an object of this Class.
     * 
     * @param schemaVariable
     *            the schema variable to capture the whole program term
     * @param terminating
     *            the termination state of this term
     * @param matchingStatement
     *            the matching statement, may be null
     * @param formula
     *            the formula to be evaluated in the post states of the program
     *            execution.
     * @throws TermException
     *             if the suffix term is illegal
     */
    private SchemaProgramTerm(@NonNull SchemaVariable schemaVariable,
            boolean terminating, @Nullable Statement matchingStatement, Term formula) throws TermException {
        super(new Term[] { formula, schemaVariable }, terminating);
        this.matchingStatement = matchingStatement;
    }

    /**
     * Gets an application term.
     * 
     * If a term with the given parameters already exists in the system, a
     * reference to it is returned instead of a freshly created one. If not, a
     * new instance is created.
     * 
     * @param schemaVariable
     *            the schema variable to capture the whole program term
     * @param terminating
     *            the termination state of this term
     * @param matchingStatement
     *            the matching statement, may be null
     * @param formula
     *            the formula to be evaluated in the post states of the program
     *            execution.
     * 
     * @return a term with the given parameters. Not necessarily freshly
     *         created.
     * @throws TermException
     *             if the suffix term is illegal
     */
    public static @NonNull SchemaProgramTerm getInst(@NonNull SchemaVariable schemaVariable,
            boolean terminating, @Nullable Statement matchingStatement, Term formula) throws TermException {
        return (SchemaProgramTerm) new SchemaProgramTerm(schemaVariable, terminating,
                matchingStatement, formula).intern();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This object is equal to another object if
     * <ol>
     * <li><code>object</code> is a {@link SchemaProgramTerm} as well.
     * <li>they both have the same schema variable
     * <li>their matching statements are equal or both null
     * </ol>
     */
    public boolean equals(@Nullable Object object) {
        if (object instanceof SchemaProgramTerm) {
            SchemaProgramTerm sch = (SchemaProgramTerm) object;
            return getSchemaVariable().equals(sch.getSchemaVariable())
                    && Util.equalOrNull(matchingStatement,
                            sch.matchingStatement);
        }
        return false;
    }

    /**
     * The content string for a schema program term is the schemavariable (<code>%s</code>)
     * followed by the matching statement <code>stm</code> like in
     * <pre>
     *    %s : stm
     * </pre>
     * or
     * <pre>
     *    %s
     * </pre>
     * 
     * if no matching statement has been provided.
     */
    protected String getContentString(boolean typed) {
        String res = getSchemaVariable().toString(false);
        if (hasMatchingStatement())
            res += ": " + matchingStatement.toString(typed);
        return res;
    }

    /**
     * Checks whether his schema term has a matching statement
     * 
     * @return true, if it has a matching statement different from null
     */
    @AssertNonNullIfTrue({"matchingStatement", "getMatchingStatement()"})
    public boolean hasMatchingStatement() {
        return matchingStatement != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.iti.pseudo.term.Term#visit(de.uka.iti.pseudo.term.TermVisitor)
     */
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * Gets the schema variable. That is always the second subterm (the first
     * being the suffix formula).
     * 
     * @return the schema variable
     */
    public @NonNull SchemaVariable getSchemaVariable() {
        return (SchemaVariable) getSubterm(1);
    }

    /**
     * Gets the matching statement.
     * 
     * @return the matching statement or null if none set
     */
    public @Nullable Statement getMatchingStatement() {
        return matchingStatement;
    }

}
