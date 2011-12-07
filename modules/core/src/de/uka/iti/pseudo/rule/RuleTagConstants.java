/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
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
     * @ivildoc "Rule tag/decisionProcedure"
     * 
     * "decisionProcedure" expects a classname as argument.
     * 
     * <p>It denotes the decision procedure plugin to instantiate.
     */
    public static final String KEY_DECISION_PROCEDURE = "decisionProcedure";

    /**
     * @ivildoc "Rule tag/timeout"
     * "timeout" expects an integer as argument.
     * 
     * <p>For decision procedure rules. The time to wait before shutting down the
     * DP.
     */
    public static final String KEY_TIMEOUT = "timeout";

    /**
     * @ivildoc "Rule tag/rewrite" "rewrite" expects a string argument.
     * 
     * <p>
     * It declares the set of rewrite rules to which a rule belongs. They list
     * of known rule sets include the following:
     * <ol>
     * <li>"updSimpl" - update simplification</li>
     * <li>"close" - rules to close goals</li>
     * <li>"concrete" - rules involving operations on constants (
     * <code>false | a</code> to <code>a</code>)</li>
     * <li>"prop simp" - propositional simplification</li>
     * <li>"fol simp" - first order simplification</li>
     * </ol>
     * Rule sets are applied in that order by the simplification strategy.
     * There are other rule sets for more specific purposes. "symbex" is used
     * for symbolic execution for instance. 
     */
    public static final String KEY_REWRITE = "rewrite";

    /**
     * @ivildoc "Rule tag/prio"
     * "prio" expects an integer as argument.
     * 
     * <p>When sorting rules, the priority is used as comparison criterion.
     */
    public static final String KEY_PRIORITY = "prio";

    /**
     * @ivildoc "Rule tag/display" 
     * 
     * "display" expects a string as argument,
     * possibly with embedded schema variables.
     * 
     * <p>
     * This string is used to annotate history entries and proof tree component
     * labels.
     * 
     * <p>The format is the following:
     * <table>
     * <tr>
     * <th>Text</th>
     * <th>Replacement</th>
     * </tr>
     * <tr>
     * <td>{%c}</td>
     * <td>The text representation of the instantiation for the schema variable,
     * "??" if not instantiated.</td>
     * </tr>
     * <tr>
     * <td>{explain %c}</td>
     * <td>The text representation of the annotation statement to which %c points
     * (if it does exists). Empty string otherwise.</td>
     * </tr>
     * <tr>
     * <td>{explainOrQuote %c}</td>
     * <td>The text representation of the annotation statement to which %c points
     * (if it does). The statement otherwise. Empty string if %c does not hold a
     * program term.</td>
     * </tr>
     * <tr>
     * <td>{property name}</td>
     * <td>The value of the named property of the rule application.</td>
     * </tr>
     * <tr>
     * <td>{upd U}</td>
     * <td>The textual representation of the value of the instantiation of the
     * schema update.</td>
     * </tr>
     * </table>
     */
    public static final String KEY_DISPLAY = "display";

    /**
     * @ivildoc "Rule tag/autoonly"
     * 
     * "autoonly" does not take an argument.
     * 
     * <p>It makes a rule invisible for manual application.
     */
    public static final String KEY_AUTOONLY = "autoonly";

    /**
     * @ivildoc "Rule tag/verbosity"
     * 
     * "verbosity" expects an integer as argument.
     * 
     * <p>It defines from which verbosity level on the rule is printed in the proof
     * component.
     */
    public static final String KEY_VERBOSITY = "verbosity";

    /**
     * @ivildoc "Rule tag/derived"
     * 
     * "derived" does not an need argument.
     * 
     * <p>It marks a rule to be inferrable by the rules preceeding it (plus
     * includes)
     */
    public static final String KEY_DERIVED_RULE = "derived";
    
    /**
     * @ivildoc "Rule tag/asAxiom"
     * 
     * "asAxiom" does not take an argument.
     * 
     * <p>It marks a rule to be translated into an axiom which allows it to
     * be translated to SMT. This is not possible for all rules.
     */
    public static final String KEY_AS_AXIOM = "asAxiom";
    
    /**
     * @ivildoc "Rule tag/fromRule"
     * 
     * "fromRule" takes a rule name as argument.
     * 
     * <p>It is added automatically to axioms which are generated automatically
     * from rules. The argument is set to the name of the originating rule.
     */
    public static final String KEY_GENERATED_AXIOM = "fromRule";
}
