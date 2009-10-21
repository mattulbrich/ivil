/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.rule;

/**
 * This is the collection of all possible tags that can be added to a rule
 * declaration.
 * 
 * Any call to {@link Rule#getProperty(String)} should be performed by
 * referencing to a constant in this pool.
 * 
 * @see Rule#getProperty(String)
 */
public final class RuleTagConstants {

    /**
     * Instantiation impossible
     */
    private RuleTagConstants() {
        throw new Error("though shalt not instantiate");
    }

    /**
     * KEY_DECISION_PROCEDURE expects a classname as argument.
     * 
     * It denotes the decision procedure plugin to instantiate.
     */
    public static final String KEY_DECISION_PROCEDURE = "decisionProcedure";

    /**
     * KEY_TIMEOUT expects an integer as argument.
     * 
     * For decision procedure rules. The time to wait before shutting down the
     * DP.
     */
    public static final String KEY_TIMEOUT = "timeout";

    /**
     * KEY_REWRITE expects a string argument.
     * 
     * It declares the class of rewrite rules to which a rule belongs.
     */
    public static final String KEY_REWRITE = "rewrite";

    /**
     * KEY_PRIORITY expects an integer as argument.
     * 
     * When sorting rules, the priority is used as comparison criterion.
     */
    public static final String KEY_PRIORITY = "prio";

    /**
     * KEY_DISPLAY expects a string as argument, possibly with embedded
     * {%schema} variables.
     * 
     * This string is used to annotate history entries and proof component
     * labels.
     */
    public static final String KEY_DISPLAY = "display";

    /**
     * KEY_AUTOONLY does not an need argument.
     * 
     * It makes a rule invisible for manual application
     */
    public static final String KEY_AUTOONLY = "autoonly";

    /**
     * KEY_VERBOSITY expects an integer as argument.
     * 
     * It defines from which verbosity level on the rule is printed in the proof
     * component.
     */
    public static final String KEY_VERBOSITY = "verbosity";
}
