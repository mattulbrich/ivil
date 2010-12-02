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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.util.Util;


// TODO: Auto-generated Javadoc
/**
 * The Class Rule encapsulates a logical rule with several elements:
 * <ol>
 * <li>Every rule has got a name.
 * <li>a find clause (located term)
 * <li>zero or more assumption clauses (located terms)
 * <li>zero or more where clauses (side conditions)
 * <li>one or more goal actions
 * <li>zero or more named properties (in a string to string map)
 * </li>
 * 
 * Rules are immutable objects.
 * 
 */
public class Rule {
    
    /**
     * new line character for pretty printing
     */
    private static final String NEWLINE = "\n";
    
    /**
     * The name of this rule
     */
    private @NonNull String name;
    
    /**
     * The set of assumptions. no null in here.
     */
    private @DeepNonNull LocatedTerm assumptions[];
    
    /**
     * The find clause.
     */
    private @Nullable LocatedTerm findClause;
    
    /**
     * The where clauses.
     */
    private @DeepNonNull WhereClause whereClauses[];
    
    /**
     * The goal actions. a non-empty array.
     */
    private @DeepNonNull GoalAction goalActions[];
    
    /**
     * The properties.
     */
    private @NonNull Map<String, String> properties;

    /**
     * The location
     */
    private @NonNull ASTLocatedElement location;
    
    /**
     * gets a property. Properties are specified using the "tag" keyword in
     * environments. If the property is not set, null is returned. If the
     * property has been defined without a value, an empty string "" is returned
     * 
     * <p>Please use a constant defined in {@link RuleTagConstants} as argument
     * to keep all sensible tags at one place.
     * 
     * @see RuleTagConstants
     * 
     * @param string
     *            name of the property to retrieve
     * @return the property if it is defined, null otherwise
     */
    public String getProperty(String string) {
        return properties.get(string);
    }

    /**
     * Gets a collection which contains the names of all defined properties for this
     * rule. The entries to this collections are different from null and can be used
     * as keys to {@link #getProperty(String)}.
     *  
     * @return an unmodifiable collection of strings.
     */
    public Collection<String> getDefinedProperties() {
    	return properties.keySet();
    }

    /**
     * Gets the name of this rule.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the location of the declaration of this rule.
     * 
     * @return the source location of this declaration.
     */
    public ASTLocatedElement getDeclaration() {
        return location;
    }

    /**
     * Gets an immutable list of all assumptions.
     * 
     * @return the assumptions as list
     */
    public @NonNull List<LocatedTerm> getAssumptions() {
        return Util.readOnlyArrayList(assumptions);
    }

    /**
     * Gets the find clause.
     * 
     * @return the find clause, may be null
     */
    public @Nullable LocatedTerm getFindClause() {
        return findClause;
    }

    /**
     * Gets an immutable list of all where clauses.
     * 
     * @return the where clauses as list
     */
    public @NonNull List<WhereClause> getWhereClauses() {
        return Util.readOnlyArrayList(whereClauses);
    }

    /**
     * Gets an immutable list of all goal actions.
     * 
     * @return the goal actions as list
     */
    public List<GoalAction> getGoalActions() {
        return Util.readOnlyArrayList(goalActions);
    }

    /**
     * Instantiates a new rule.
     * 
     * @param name
     *            rule's name
     * @param assumes
     *            the assumptions
     * @param find
     *            the find clause
     * @param wheres
     *            the where clauses
     * @param actions
     *            the goal action, must not be the empty list
     * @param properties
     *            the properties
     * @param location
     *            the location of the declaration in the sources
     * 
     * @throws RuleException
     *             if the elements do not compose a valid rule.
     */
    public Rule(@NonNull String name, @NonNull List<LocatedTerm> assumes, 
            @Nullable LocatedTerm find, @NonNull List<WhereClause> wheres,
            @NonNull List<GoalAction> actions, 
            @NonNull Map<String, String> properties, 
            @NonNull ASTLocatedElement location)
            throws RuleException {
        this.name = name;
        this.assumptions = Util.listToArray(assumes, LocatedTerm.class);
        this.findClause = find;
        this.whereClauses = Util.listToArray(wheres, WhereClause.class);
        this.goalActions = Util.listToArray(actions, GoalAction.class);
        this.properties = properties;
        this.location = location;
        
        checkRule();
    }

    /*
     * Check rule. raise an exception if the elements are not valid for a rule.
     */
    private void checkRule() throws RuleException {
        if(getGoalActions().size() == 0)
            throw new RuleException("Rule has no goal action");

        // XXX rule checking!!
        // closegoal is empty, newgoal has no replace : checked in GoalAction
        // remove only if find is top level
        for (GoalAction goalAction : getGoalActions()) {
            if(goalAction.isRemoveOriginalTerm() && 
                    (getFindClause() == null ||
                            getFindClause().getMatchingLocation() == MatchingLocation.BOTH)) {
                throw new RuleException("Goal action contains remove element where find is not present or not top level");
            }
            
            if(goalAction.getReplaceWith() != null) {
                if(getFindClause() == null)
                    throw new RuleException("Find-less rules must not have a replace clause");
                if(!getFindClause().getTerm().getType().equals(goalAction.getReplaceWith().getType()))
                    throw new RuleException("Find clause and replace clause must have the same type");
            }
        }
        
        // schema variables to always have same type:
        RuleSchemaConsistencyChecker.check(this);
    }
    
    /**
     * Dump the rule to Stdout.
     */
    public void dump() {

        System.out.println("  Rule " + name);
        
        if(findClause != null) {
            System.out.print("    Find: ");
            System.out.println(findClause);
        }
        
        System.out.println("    Assumptions:");
        for (LocatedTerm lt : assumptions) {
            System.out.println("      " + lt);
        }
        
        System.out.println("    Where clauses:");
        for (WhereClause wc : whereClauses) {
            System.out.println("      " + wc);
        }
        
        System.out.println("    Actions:");
        for (GoalAction ga : goalActions) {
            ga.dump();
        }
    }
    
    public String toString() {
        return "Rule[" + name + "]";
    }

}
