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

import java.util.List;
import java.util.Map;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;

/**
 * RuleApplication defines an interface to a persistently stored rule
 * application, i.e., it records
 * <ol>
 * <li>which {@link Rule} is to be applied
 * <li>to which open goal of a {@link Proof} (see
 * {@link Proof#getGoalByNumber(int)})
 * <li>It provides a {@link TermSelector} indicating the <i>find</i> clause of
 * the rule,
 * <li>as well as a list of {@code TermSelector}s indicating the <i>assume</i>
 * clauses to be used.
 * <li>An instantiation is given for all relevant schema entities, that is:
 * schema-variables, schema-updates and schema-types.
 * <li>Finally, user-defined properties can be provided.
 * </ol>
 *
 * An implementing class may store the data in a mutable fashion. The values are
 * then copied to an unmodifiable instance when using the application, however.
 *
 * <p>
 * The class {@link ImmutableRuleApplication} is the implementation that cannot
 * be changed after creation.
 */
public interface RuleApplication {

    /**
     * Retrieves the rule which is to be applied
     *
     * @return a reference to the rule
     */
    public @NonNull Rule getRule();

    /**
     * Checks if this implementation allows changes in its properties.
     *
     * @return <code>true</code> iff the properties can be changed.
     */
    public boolean hasMutableProperties();

    /**
     * Retrieves the goal to apply the rule on. The node must be a leaf in a
     * proof: It must not have been pruned and must not have children yet.
     *
     * @see Proof#getGoalByNumber(int)
     * @see ProofNode#getNumber()
     * @return a non-negative integer
     */
    public @NonNull ProofNode getProofNode();

    /**
     * Retrieves the selector which points to the find clause which is to be
     * used.
     *
     * <p>
     * This may return <code>false</code> if the rule to apply is findless.
     *
     * @see Rule#getFindClause()
     * @return the find selector
     */
    public @Nullable TermSelector getFindSelector();

    /**
     * Retrieves all assume selectors as a list.
     *
     * <p>The following invariant holds:
     * <pre>
     *   getRule().getAssumptions().size() == getAssumeSelectors().size()
     * </pre>
     *
     * @see Rule#getAssumptions()
     * @return a list of selectors with a fixed length
     */
    public @DeepNonNull List<TermSelector> getAssumeSelectors();

    /**
     * Gets a mapping which relates schema variables to terms.
     *
     * @return the schema variable mapping
     */
    public @DeepNonNull Map<String, Term> getSchemaVariableMapping();

    /**
     * Gets a mapping which relates schema updates to updates.
     *
     * @return the schema update mapping
     */
    public @DeepNonNull Map<String, Update> getSchemaUpdateMapping();

    /**
     * Gets a mapping which relates type variables to types.
     *
     * <p><b>Note:</b>The keys into this map are type variable names w/o a
     * leading prime (').
     *
     * @return the type variable mapping
     */
    public @DeepNonNull Map<String, Type> getTypeVariableMapping();

    /**
     * Gets the additional properties for this rule application.
     *
     * <p>
     * The properties that are stored herein depend heavily on the applied rule.
     * Usually these are meta data needed during the application of
     * {@link MetaFunction}s.
     *
     * <p>
     * If {@link #hasMutableProperties()} returns <code>true</code>, the
     * returned map must implement the modifying methods.
     *
     * @return a map from strings to strings
     */
    public @DeepNonNull Map<String, String> getProperties();
}