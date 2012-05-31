/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;

/**
 * The where condition KnownFormula can be used to reintroduce a formula which
 * had been present on the sequent in an earlier proof state.
 *
 * It may have disappeared by hiding or rule application, ...
 *
 * <p>
 * We will not use this rule with matching but by separate UI/automation means.
 * These means need to provide the rule application with a property
 *
 * <h3>Application</h3>
 *
 * <pre>
 * rule unhide_left
 *   where knownFormula %b, %left
 *   add %b |-
 *   tags autoonly
 *        display "Unhide formula from {property knownFormula}"
 * </pre>
 *
 * The same (mutatis mutandis) for {@code unhide_right}.
 *
 * <p>
 * When applying this rule, we need to ensure that the property
 * {@value #KNOWN_FORMULA_PROPERY} gets set to a value indicating a sequent
 * position. The format of the string is:
 *
 * <pre>
 * {@literal <proof-node-number> : [S|A] . <index-of-formula>}
 * </pre>
 *
 * with S/A indicating succeedent/antecedent. For example: {@code "4711:S.3"}
 * points to the forth(!) formula of the succedent of sequent number 4711.
 *
 * <p>
 * It is the obligation of
 * {@link #check(Term[], Term[], RuleApplication, Environment)} to check that
 * the actual first argument is set to this value. Additionally we check that
 * the node is a successor of the target note and that only
 * {@link GoalAction.Kind#COPY} actions have been performed (NEW would be
 * illegal).
 *
 * <h3>Justification</h3>
 * The rules
 *
 * <pre>
 * <tt>
 *   A,A, Gamma |- Delta
 *  =====================
 *   A, Gamma |- Delta
 * </tt>
 * </pre>
 *
 * and
 *
 * <pre>
 * <tt>
 *   Gamma |- Delta, B,B
 *  =====================
 *   Gamma |- Delta, B
 * </tt>
 * </pre>
 *
 * are sound (and confluent) sequent calculus rules. We can, hence, "clone" any
 * formula on the sequent and take it along through all non-restarting rule
 * applications. This justifies this where condition.
 *
 * <h3>COmments</h3>
 * We could drop the necessity to instantiate the first argument by providing an
 * implementation for
 * {@link #addInstantiations(de.uka.iti.pseudo.term.creation.TermMatcher, Term[])}
 * . However, the outer mechanism has to hold the formula anyway. ...
 */
public class KnownFormula extends WhereCondition {

    // TODO Piut this into RuleConstants and explain it in ivildoc there
    public static final String KNOWN_FORMULA_PROPERY = "knownFormula";

    public KnownFormula() {
        super("knownFormula");
    }

    @Override
    public boolean check(Term[] formalArguments, Term[] actualArguments,
            RuleApplication ruleApp, Environment env) throws RuleException {

        //
        // infos from property
        Pair<Integer, TermSelector> pair =
            splitProperty(ruleApp.getProperties().get(KNOWN_FORMULA_PROPERY));
        int nodeNo = pair.fst();
        TermSelector tsel = pair.snd();

        //
        // check that selector matches marker
        if(!matchMarker(tsel, formalArguments[1])) {
            return false;
        }

        //
        // find that node
        ProofNode node = findNode(nodeNo, ruleApp.getProofNode());
        if(node == null) {
            // TODO or an exception?
            return false;
        }

        assert node.getNumber() == nodeNo;

        //
        // instantiate and check it.
        Term theFormula;
        try {
            theFormula = tsel.selectSubterm(node.getSequent());
        } catch (ProofException e) {
            return false;
        }

        if(!theFormula.equals(actualArguments[0])) {
            return false;
        }

        // everything ok.
        return true;
    }

    private boolean matchMarker(TermSelector tsel, Term term) {
        // Antecedent is left (only %LEFT/%RIGHT allowed)
        boolean onAnte = term.toString(false).equals("%LEFT");
        return onAnte == tsel.isAntecedent();
    }

    /**
     * Starting from {@code start} walk the proof tree upwards and find the node
     * searched for. Stop at non-copy goals.
     *
     * @param nodeNo
     *            node to find
     * @param node
     *            node start with
     * @return null, if not found/reachable, otherwise the node with the given
     *         index.
     */
    private @Nullable ProofNode findNode(int nodeNo, ProofNode node) {

        while(true) {
            if(node == null) {
                // over the root
                return null;
            }

            if(node.getNumber() == nodeNo) {
                // found it!
                return node;
            }

            // now look at parent
            ProofNode parent = node.getParent();
            // and node's index in its actions
            int myIndex = parent.getChildren().indexOf(node);
            // and the action that lead to node's creation
            GoalAction action =
                parent.getAppliedRuleApp().getRule().getGoalActions().get(myIndex);
            GoalAction.Kind kind = action.getKind();

            // only COPY goals.
            if(kind != GoalAction.Kind.COPY) {
                return null;
            }

            node = parent;
        }
    }

    /**
     * Split the set property of the rule application into proof node index and
     * term selector.
     *
     * @param property
     *            the property form the rule app.
     *
     * @return a pair of non-null elements.
     *
     * @throws RuleException
     *             if the property is not there or illegally formated
     */
    private Pair<Integer, TermSelector> splitProperty(String property) throws RuleException {

        if(property == null) {
            throw new RuleException("Property " + property + "not set on rule application");
        }

        try {
            String[] parts = property.split(":");
            if(parts.length != 2) {
                throw new IllegalArgumentException("Wrong number of parts, expected 2");
            }

            // throws NumberFormatException:
            int node = Util.parseUnsignedInt(parts[0]);
            // throws FormatException:
            TermSelector tsel = new TermSelector(parts[1]);

            if(!tsel.isToplevel()) {
                throw new IllegalArgumentException("Wrong term selector, expected top level");
            }

            return Pair.make(node, tsel);

        } catch(Exception e) {
            throw new RuleException("Illegally formated property: " + property, e);
        }
    }

    /**
     * syntactically we need two arguments: First the formula to be reintroduced
     * then a marker of antecedent/succedent. We accept "%LEFT" for antecedent
     * and "%RIGHT" for succedent.
     */
    @Override
    public void checkSyntax(Term[] arguments) throws RuleException {
        if (arguments.length != 2) {
            throw new RuleException("knownFormula expects two arguments");
        }

        Term arg = arguments[0];
        if(!Environment.getBoolType().equals(arg.getType())) {
            throw new RuleException("knownFormula expects a boolean term as first argument");
        }

        String location = arguments[1].toString(false);
        if (!"%RIGHT".equals(location) && !"%LEFT".equals(location)) {
            throw new RuleException("knownFormula expects either %LEFT or %RIGHT as second argument, was " + location);
        }
    }

}
