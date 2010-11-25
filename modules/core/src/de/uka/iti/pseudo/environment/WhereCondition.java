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
package de.uka.iti.pseudo.environment;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;

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
 * <li> {@link #check(Term[], Term[], RuleApplication, ProofNode, Environment)}
 * is used to check whether a where clause allows the application of a rule or
 * inhibits it given a schema variable instantiation.
 * </ol>
 * 
 * @see Rule
 * @see WhereClause
 */
public abstract class WhereCondition implements Mappable {

    //////////////////////////////////////
    // Static material
    
    /**
     * The name under which plugins for this service have to
     * be registered.
     */
    public static final String SERVICE_NAME = "whereCondition";

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
    public static @Nullable WhereCondition getWhereCondition(@NonNull Environment env, @NonNull String name)
            throws EnvironmentException {
        return env.getPluginManager().getPlugin(SERVICE_NAME, WhereCondition.class, name);
    }
    
    //////////////////////////////////////
    // Instance material
    
    /**
     * The name of this condition
     */
    private String name;
    
    /**
     * Create a new where condition.
     * 
     * @param name name of the condition
     */
    protected WhereCondition(@NonNull String name) {
        this.name = name;
    }

    /**
     * Gets the name of this condition
     * 
     * @return the name of this condition
     */
    public @NonNull String getName() {
        return name;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>For where conditions, the name is the unique key.
     */
    @Override 
    public @NonNull Object getKey() {
        return getName();
    }
    
    /**
     * Any implementation must provide this method to check the syntax of 
     * where clauses.
     * 
     * <p>This method is called when parsing rules. It should check type and 
     * number of the <b>formal</b> arguments and similar syntactical things.
     * 
     * <p>The array of arguments are the arguments that are applied to
     * the condition in the rule definition, without any instantiations
     * made. 
     * 
     * @param arguments the terms to which the condition is to be applied.
     * 
     * @throws RuleException if syntax is incorrect
     */
    public abstract void checkSyntax(@DeepNonNull Term[] arguments) throws RuleException;

    /**
     * Any implementation must provide this method to check the validity of a
     * where clause under a schema instantiation.
     * 
     * <p>
     * This method is called when trying to apply rules. It checks the
     * <b>actual</b> arguments. The actual arguments arise from the formal
     * arguments by applying the instantiation of the provided rule application.
     * 
     * <p>It should return <code>false</code> if the actual arguments inhibit
     * the application of this rule instance. It should throw an exception if
     * the parameters are not of the form in which they were expected to be.
     * 
     * <p>
     * The implementation can use the provided arguments to call queries on
     * them, but must not change them.
     * 
     * @param formalArguments
     *            the formal arguments of the where clause (the same as given to
     *            {@link #checkSyntax(Term[])}.
     * @param actualArguments
     *            the actual arguments arise by applying the ruleApp's
     *            instantiation to the formal arguments.
     * @param ruleApp
     *            the rule application which is to be checked against
     * @param goal
     *            the goal upon which the rule is to be applied
     * @param env
     *            the environment in which the condition is evaluated
     * 
     * @return <code>true</code> iff the where clause passed the test and does
     *         not inhibit the rule application
     * 
     * @throws RuleException
     *            if inacceptable parameters are passed. 
     */
    public abstract boolean check(Term[] formalArguments, Term[] actualArguments, 
            RuleApplication ruleApp, ProofNode goal, Environment env) throws RuleException;

}
