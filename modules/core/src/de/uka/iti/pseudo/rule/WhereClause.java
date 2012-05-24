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
package de.uka.iti.pseudo.rule;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.util.Util;

/**
 * A where clause is the composition of a {@link WhereCondition} and a number of
 * argument terms. It used within a rule to encode a condition under which the
 * rule is applicable.
 *
 * @see WhereCondition
 * @see Rule
 */
public class WhereClause {

    /**
     * The where condition employed in this clause
     */
    private final WhereCondition whereCondition;

    /**
     * A flag indicating whether the condition should check for success or failure.
     */
    private final boolean inverted;

    /**
     * The arguments applied to the condition
     */
    private @NonNull final Term[] arguments;

    /**
     * Instantiates a new where clause.
     *
     * <p>
     * The terms array is referenced to directly and not copied beforehand!
     *
     * @param where
     *            the where condition
     * @param inverted
     *            indicates whether the condition is to <b>fail</b> rather
     *            than to succeed.
     * @param terms
     *            the terms on which the condition is applied
     *
     * @throws RuleException
     *             if the syntax check of the condition fails for the arguments
     *             terms.
     */
    public WhereClause(WhereCondition where, boolean inverted, Term[] terms) throws RuleException {
        this.arguments = terms;
        this.whereCondition = where;
        this.inverted =inverted;

        where.checkSyntax(arguments);
    }

    /**
     * Gets the arguments of the clause as a list.
     *
     * @return the arguments as unmodifiable list.
     */
    public List<Term> getArguments() {
        return Util.readOnlyArrayList(arguments);
    }

    /**
     * Gets the where condition of this clause.
     *
     * @return the where condition used in this clause.
     */
    public WhereCondition getWhereCondition() {
        return whereCondition;
    }

    /**
     * Returns whether this where clause inverts results computed by the where
     * condition.
     *
     * @return <code>true</code> for inverted, <code>false</code> for straight.
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * prints the where condition followed by a commatised list of the formal
     * arguments.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(whereCondition.getName()).append(" ").append(
                Util.commatize(getArguments()));
        return sb.toString();
    }

    /**
     * Test this clause against a rule application context.
     *
     * <p>
     * If the test succeeds and the the clause allows the rule application, true
     * is returned. If the paramters disallow the application, false is
     * returned. The method may throw an exception if the arguments to the
     * condition do not correspond to the expected form.
     *
     * <p>
     * The instantiation and the rule application must be compatible.
     *
     * @param inst
     *            the instantiation of the schema entities
     * @param ruleApp
     *            the rule application under consideration
     * @param env
     *            the environment of this proof.
     *
     * @return true, if the instantiations render the clause successful
     *
     * @throws RuleException
     *             the arguments to the condition do not correspond to the
     *             expected form.
     */
    public boolean applyTo(TermInstantiator inst, RuleApplication ruleApp, Environment env) throws RuleException {

        Term actualArgs[];
        try {
            actualArgs = new Term[arguments.length];
            for (int i = 0; i < actualArgs.length; i++) {
                actualArgs[i] = inst.instantiate(arguments[i]);
            }

        } catch (TermException e) {
            throw new RuleException("Exception during instantiation", e);
        }

        // This is the same as (check() && !inverted) || (!check() && inverted)
        return whereCondition.check(arguments, actualArgs, ruleApp, env) != inverted;
    }

    /**
     * Allow this clause to actively instantiate schema entities.
     *
     * <p>
     * This is an action and does not provide a possibilty to feedback
     * information. Failures are to be reported by
     * {@link #applyTo(TermInstantiator, RuleApplication, ProofNode, Environment)}.
     *
     * <p>
     * This is not called when applying a rule application to a node. Then the
     * instatiations need to have been done. It is called during finding of
     * applications, however.
     *
     * @param termMatcher
     *            the object on which the matchings and active instantiations
     *            can be performed.
     * @param ruleApp
     *            The rule application in whose context the rule is applied.
     * @param env The environment in whose context the rule is applied.
     * @throws RuleException
     */
    public void addInstantiations(TermMatcher termMatcher,
            RuleApplication ruleApp, Environment env) throws RuleException {

        whereCondition.addInstantiations(termMatcher, arguments);
    }

}
