/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import java.util.HashMap;
import java.util.Map;

import nonnull.NonNull;

import com.sun.istack.internal.Nullable;

import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.where.Interactive;
import de.uka.iti.pseudo.rule.where.NewSkolem;
import de.uka.iti.pseudo.rule.where.NotFreeIn;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermUnification;


/**
 * The Class WhereCondition.
 */
public abstract class WhereCondition {

    //////////////////////////////////////
    // Static material
    
    /**
     * The condition table provides linear lookup possibility for 
     * where conditions.
     */
    private static Map<String, WhereCondition> whereConditionTable =
        new HashMap<String, WhereCondition>();
    
    /**
     * The array of all known conditions.
     * Please add your WhereCondition here.
     * 
     * TODO possibly do this this services and providers etc.
     */
    private static final WhereCondition CONDITIONS[] =
    {
        new NotFreeIn(),
        new NewSkolem(),
        new Interactive()
    };
    
    /*
     * add all conditions from CONDITIONS to the hash table
     */
    static {
        for (WhereCondition wc : CONDITIONS) {
            String name = wc.getName();
            if(whereConditionTable.get(name) != null)
                System.err.println("Warning: where condition " + name + " registered more than once.");
            whereConditionTable.put(name, wc);
        }
    }

    /**
     * retrieve a where condition of a given name
     * 
     * @param name name of the condition
     * 
     * @return the where condition if there is one by this name, null otherwise
     */
    public static @Nullable WhereCondition getWhereCondition(@NonNull String name) {
        return whereConditionTable.get(name);
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
    
 // TODO: Auto-generated Javadoc
  //TODO DOC
    
    
    public abstract boolean applyTo(Term[] arguments, TermUnification mc,
            RuleApplication ruleApp, ProofNode goal, Environment env, 
            Map<String, String> properties, boolean commit) throws RuleException;
    
    /**
     * Was imported.
     * 
     * @param whereClause the where clause
     * @param env the env
     * @param properties the properties
     * 
     * @throws RuleException the rule exception
     */
    public void wasImported(Term[] formalArguments, Environment env, Map<String, String> properties) throws RuleException {
        // default behaviour is to do nothing.
    }
    
    
    public abstract void verify(Term[] formalArguments, Term[] actualArguments, Map<String, String> properties) throws RuleException;

}
