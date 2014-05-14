/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.FormatException;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;

/**
 * The Class RuleProofHint implements a proof hint which applies one rule.
 *
 * ivildoc "Proof hint/focus"
 *
 * <h2>Proof hint <code>focus</code></h2>
 *
 * This hint allows you to pick one formula from a sequent to prove it
 * self-contained.
 *
 * <h3>Arguments</h3> Takes at most one sequent formula locators which matches
 * the regular expression <tt>(S|A).[0-9]+</tt>. <tt>A</tt> denoting antecedent
 * and <tt>S</tt> succedent. The number means the number of the formula on the
 * sequent (0 is first).
 *
 * <p>
 * If no argument is given, the focus is on the <tt>S.0</tt>, i.e. the first
 * formula in the succedent.
 *
 * <p>
 * Formulas are focused on using the <tt>focus_left</tt> and
 * <tt>focus_right</tt> rules.
 *
 * <h3>Example</h3>
 *
 * <pre>
 * assert x*x >= 0 ; "as a lemma by §focus"
 * </pre>
 */
public class FocusProofHint implements ProofHint {

    @Override
    public String getName() {
        return "focus";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new FocusHintAppFinder(env, arguments);
    }
}

class FocusHintAppFinder extends HintRuleAppFinder {

    private final Environment env;
    private final TermSelector termSelector;
    private final Rule focusRightRule;
    private final Rule focusLeftRule;

    public FocusHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;

        if(arguments.size() > 2) {
            throw new StrategyException("The proofhint 'focus' expects at most argument");
        }

        assert "focus".equals(arguments.get(0));

        try {
            if(arguments.size() == 1) {
                termSelector = new TermSelector("S.0");
            } else {
                termSelector = new TermSelector(arguments.get(1));
            }
        } catch (FormatException e) {
            throw new StrategyException("Illegally formatted termselector: " + arguments, e);
        }

        // TODO null checks
        focusLeftRule = env.getRule("focus_left");
        focusRightRule = env.getRule("focus_right");
    }

    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {

        // Only applicable to the reasonNode, not later
        if(node != reasonNode) {
            return null;
        }

        Rule rule = termSelector.isAntecedent() ? focusLeftRule : focusRightRule;

        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setProofNode(node);
        ram.setFindSelector(termSelector);
        ram.setRule(rule);
        try {
            ram.matchInstantiations();
        } catch (ProofException e) {
            throw new StrategyException("Failed to instantiate rule " + rule, e);
        }
        return ram;
    }

}