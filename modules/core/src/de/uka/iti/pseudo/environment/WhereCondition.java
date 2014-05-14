/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMatcher;

/**
 * Where conditions are used to construct where clauses which are part of rule
 * declarations. They describe conditions on the matched terms under which the
 * rule may be applied.
 *
 * <p>
 * Where conditions have two check methods:
 * <ol>
 * <li> {@link #checkSyntax(Term[])} which is called when creating a where
 * clause. An implementation should check the number and static type of
 * arguments, etc.
 * <li> {@link #check(Term[], Term[], RuleApplication, Environment)} is used to
 * check whether a where clause allows the application of a rule or inhibits it
 * given a schema variable instantiation.
 * </ol>
 *
 * <h4>Immutable rule applications</h4>
 *
 * The {@link #check(Term[], Term[], RuleApplication, Environment)} method
 * receives a {@link RuleApplication} as argument, but must not modify it.
 *
 * <h4>Active where conditions</h4>
 *
 * An active where condition can add new schema instantiations to the context.
 * Existing schema instantiations will not be changed, only new entries added.
 * These additional instantiations happen in
 * {@link #addInstantiations(TermMatcher, Term[])}. An implementation must not
 * modify existing entries.
 *
 * @ivildoc "Where condition"
 *
 * <h1>Where conditions</h1>
 *
 * Where conditions are used to formulate constraints von schema instantiations
 * in rule definitions. They describe conditions on the matched terms under
 * which the rule may be applied.
 *
 * <p>
 * Where conditions can take arguments, but do not have to. For some conditions,
 * <i>marker arguments</i> (schema variables with special names) can be used to
 * specify a particular behaviour of a condition.
 *
 * <h3>Example</h3>
 *
 * The where condition<br/>
 * <tt>&nbsp;&nbsp;&nbsp;where intLiteral %a</tt><br/>
 *
 * in a rule makes the rule only applicable if the schema variable %a is
 * instantiated with an integer literal.
 *
 * <h2>Active where conditions</h2>
 *
 * Where conditions cannot only check the instantiation context but also act and
 * actively modify it.
 *
 * <p>
 * An active where condition can add new schema instantiations to the context.
 * Existing schema instantiations will not be changed, only new entries added.
 * Please note that active conditions can also fail and not accept an
 * instantiation.
 *
 * <p>
 * The documentation will point out which parameters are active, and which are
 * passive. Future syntax changes may make the distinction clearer.
 *
 * <h3>Example</h3>
 *
 * The where condition<br/>
 * <tt>&nbsp;&nbsp;&nbsp;freshVar %z, %condition</tt><br/>
 *
 * instantiates <code>%z</code> with a variable of the same type which does not
 * appear (bound or unbound) in the instantiation of <code>%condition</code>.
 *
 * @see Rule
 * @see WhereClause
 */
public abstract class WhereCondition implements Named {

    // ////////////////////////////////////
    // Static material

    /**
     * The name under which plugins for this service have to be registered.
     */
    public static final String SERVICE_NAME = "whereCondition";

    // ////////////////////////////////////
    // Instance material

    /**
     * The name of this condition.
     */
    private final String name;

    /**
     * Create a new where condition.
     *
     * @param name
     *            name of the condition
     */
    protected WhereCondition(@NonNull String name) {
        this.name = name;
    }

    /**
     * Retrieve a where condition from an environment.
     *
     * The plugin manager of the given environment is asked to get the where
     * condition of the given name.
     *
     * @param env
     *            the environment to retrieve the plugin manager from
     * @param name
     *            the name of the where condition to be looked up
     *
     * @return the where condition, or null if not found
     *
     * @throws EnvironmentException
     *             if the plugin manager fails.
     */
    public static @Nullable WhereCondition getWhereCondition(
            @NonNull Environment env, @NonNull String name)
                    throws EnvironmentException {

        return env.getPluginManager().getPlugin(SERVICE_NAME,
                WhereCondition.class, name);
    }

    /**
     * Gets the name of this condition.
     *
     * @return the name of this condition
     */
    @Override
    public @NonNull
    String getName() {
        return name;
    }

    /**
     * Any implementation must provide this method to check the syntax of where
     * clauses.
     *
     * <p>
     * This method is called when parsing rules. It should check type and number
     * of the <b>formal</b> arguments and similar syntactical things.
     *
     * <p>
     * The array of arguments are the arguments that are applied to the
     * condition in the rule definition, without any instantiations made.
     *
     * @param arguments
     *            the terms to which the condition is to be applied.
     *
     * @throws RuleException
     *             if syntax is incorrect
     */
    public abstract void checkSyntax(@DeepNonNull Term[] arguments)
        throws RuleException;

    /**
     * Any implementation must provide this method to check the validity of a
     * where clause under a schema instantiation.
     *
     * <p>
     * This method is called when trying to apply rules. It checks the
     * <b>actual</b> arguments. The actual arguments arise from the formal
     * arguments by applying the instantiation of the provided rule application.
     *
     * <p>
     * It should return <code>false</code> if the actual arguments inhibit the
     * application of this rule instance. It should throw an exception if the
     * parameters are not of the form in which they were expected to be.
     *
     * <p>
     * The implementation can use the provided arguments to call queries on
     * them, but must not change them.
     *
     * <p>
     * <i>The argument goal is no longer needed since the goal can be extracted
     * from the rule application.</i>
     *
     * @param formalArguments
     *            the formal arguments of the where clause (the same as given to
     *            {@link #checkSyntax(Term[])}.
     * @param actualArguments
     *            the actual arguments arise by applying the ruleApp's
     *            instantiation to the formal arguments.
     * @param ruleApp
     *            the rule application which is to be checked against
     * @param env
     *            the environment in which the condition is evaluated
     *
     * @return <code>true</code> iff the where clause passed the test and does
     *         not inhibit the rule application
     *
     * @throws RuleException
     *             if inacceptable parameters are passed.
     */
    public abstract boolean check(Term[] formalArguments,
            Term[] actualArguments,
            RuleApplication ruleApp, Environment env) throws RuleException;

    /**
     * (Possibly) adds instantiations to the term matching context.
     *
     * <p>
     * Before being checked, all conditions are given the possibility to
     * contribute to the instantiation of schema entities.
     *
     * <p>
     * Since most where conditions will not be active, the actualArguments are
     * not provided and must be (if needed) computed using termMatcher.
     * Exceptions should <b>NOT</b> be thrown if the condition check fails. That
     * should be done by
     * {@link #check(Term[], Term[], RuleApplication, Environment)}. If a schema
     * entity is already instantiated, do ensure not to try to overwrite that
     * result; have it checked later.
     *
     * @param termMatcher
     *            the term matching object
     * @param arguments
     *            the formal arguments of the condition
     * @throws RuleException
     *             if the matching unexpectedly fails
     */
    public void addInstantiations(TermMatcher termMatcher, Term[] arguments)
        throws RuleException {
        // do nothing by default.
        // only active where conditions need to override this.
    }

}
