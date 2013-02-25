/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
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
     * <h2>Rule tag <tt>decisionProcedure</tt></h2>
     *
     * "decisionProcedure" expects a classname as argument.
     *
     * <p>It denotes the decision procedure plugin to instantiate.
     */
    public static final String KEY_DECISION_PROCEDURE = "decisionProcedure";

    /**
     * @ivildoc "Rule tag/timeout"
     * <h2>Rule tag <tt>timeout</tt></h2>
     *
     * "timeout" expects an integer as argument.
     *
     * <p>For decision procedure rules. The time to wait before shutting down the
     * DP.
     */
    public static final String KEY_TIMEOUT = "timeout";

    /**
     * @ivildoc "Rule tag/additionalParam"
     * <h2>Rule tag <tt>additionalParam</tt></h2>
     *
     * "additionalParam" expects a string as argument.
     *
     * <p>The format of the string depends on the DP to be used.
     * It is given to the DP on the command line.
     */
    public static final Object KEY_DECPROC_PARAMETERS = "additionalParams";

    /**
     * @ivildoc "Rule tag/rewrite"
     *
     * <h2>Rule tag <tt>rewrite</tt></h2>
     *
     * "rewrite" expects a string argument.
     *
     * <p>
     * It declares the set of rewrite rules to which a rule belongs. They list
     * of known rule sets include the following:
     * <ol>
     * <li>"updSimpl" - update simplification</li>
     * <li>"close" - rules to close goals</li>
     * <li>"concrete" - rules involving operations on constants (
     * <tt>false | a</tt> to <tt>a</tt>)</li>
     * <li>"prop simp" - propositional simplification</li>
     * <li>"fol simp" - first order simplification</li>
     * </ol>
     * Rule sets are applied in that order by the simplification strategy.
     * There are other rule sets for more specific purposes. "symbex" is used
     * for symbolic execution for instance.
     */
    public static final String KEY_REWRITE = "rewrite";

    /**
     * @ivildoc "Rule tag/prio" "prio" expects an integer as argument.
     *
     * <h2>Rule tag <tt>prio</tt></h2>
     *          <p>
     *          When sorting rules, the priority is used as comparison
     *          criterion.
     *          <p>
     *          Priorities have to be between 100 and 999, where higher priority
     *          means the rule is likelier to be applied.
     */
    public static final String KEY_PRIORITY = "prio";

    /**
     * @ivildoc "Rule tag/display"
     *
     * <h2>Rule tag <tt>display</tt></h2>
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
     * <h2>Rule tag <tt>autoonly</tt></h2>
     *
     * "autoonly" does not take an argument.
     *
     * <p>It makes a rule invisible for manual application.
     */
    public static final String KEY_AUTOONLY = "autoonly";

    /**
     * @ivildoc "Rule tag/verbosity"
     *
     * <h2>Rule tag <tt>verbosity</tt></h2>
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
     * <h2>Rule tag <tt>derived</tt></h2>
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
     * <h2>Rule tag <tt>asAxiom</tt></h2>
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
     * <h2>Rule tag <tt>fromRule</tt></h2>
     *
     * "fromRule" takes a rule name as argument.
     *
     * <p>It is added automatically to axioms which are generated automatically
     * from rules. The argument is set to the name of the originating rule.
     */
    public static final String KEY_GENERATED_AXIOM = "fromRule";

    /**
     * @ivildoc "Rule tag/hintsOnBranches"
     *
     * <h2>Rule tag <tt>hintsOnBranches</tt></h2>
     *
     * "hintsOnBranches" takes a comma separated list of integer numbers as
     * argument. It is used by the proof hint strategy to decide on which child
     * branches hints are to be applied. The first child branch has number "0".
     *
     */
    public static final String HINTS_ON_BRANCHES = "hintsOnBranches";

    /**
     * @ivildoc "Rule tag/excludeFromDP"
     *
     * <h2> Rule tag <tt>excludeFromDP</tt></h2>
     *
     * An axiom can be excluded from the translation to the decision procedure by
     * adding this tag. It can still be applied interactively or brought to the
     * sequent by an <a href="ivil:/Proof hint/axiom">axiom proof hint</a>.
     */
    public static final String EXCLUDE_FROM_DP = "excludeFromDP";
}
