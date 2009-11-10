/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;


// TODO: Auto-generated Javadoc
//TODO DOC

/**
 * The Class WhereCondition.
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
     * The name.
     */
    private String name;
    
    /**
     * Instantiates a new where condition.
     * 
     * @param name the name
     */
    protected WhereCondition(@NonNull String name) {
        this.name = name;
    }

    /**
     * Gets the name of this condition
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>For where conditions, the name is the unique key
     */
    @Override public Object getKey() {
        return getName();
    }
    
    /**
     * Any implementation must provide this method to check the syntax of 
     * where clauses.
     * 
     * <p>This method is called when parsing rules. It should check type and 
     * number of arguments and similar syntactical things.
     * 
     * <p>The array of arguments are the arguments that are applied to
     * the condition in the rule definition, without any instantiations
     * made. 
     * 
     * @param arguments the terms to which the condition is to be applied.
     * 
     * @throws RuleException if syntax is incorrect
     */
    public abstract void checkSyntax(Term[] arguments) throws RuleException;
    
    
    
    public abstract boolean check(Term[] formalArguments, Term[] actualArguments, 
            RuleApplication ruleApp, ProofNode goal, Environment env) throws RuleException;

}
