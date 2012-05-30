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

import java.util.LinkedList;
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
import de.uka.iti.pseudo.term.Sequent;

/**
 * The Class PickProofHint implements a proof hint which applies the cut rule.
 * 
 * @ivildoc "Proof hint/pick"
 * 
 * <h2>Proof hint <code>pick</code></h2>
 * 
 * This hint allows you to remove formulas from a sequent and to pick
 * only some of them.
 * 
 * <h3>Arguments</h3>
 * Takes one or more sequent formula locators which match the regular expression
 * <tt>(S|A).[0-9]+</tt>. <tt>A</tt> denoting antecedent and <tt>S</tt> succedent.
 * The number means the number of the formula on the sequent (0 is first). 
 * 
 * <p> Formulas are removed using the <tt>hide_left</tt> and <tt>hide_right</tt>
 * rules.
 * 
 * <h3>Example</h3>
 * <pre>
 * assert x*x >= 0 ; "first two imply goal §(pick A0 A1 S1)"
 * </pre>
 */
public class PickProofHint implements ProofHint {

    @Override
    public String getKey() {
        return "pick";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new PickHintAppFinder(env, arguments);
    }
}

/**
 * This implementation applies the cut rule.
 */
class PickHintAppFinder extends HintRuleAppFinder {

    private final Environment env;
    private LinkedList<TermSelector> toRemoveList = new LinkedList<TermSelector>();
    private Rule hideLeftRule;
    private Rule hideRightRule;

    public PickHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;

        if(arguments.size() < 2) {
            throw new StrategyException("The proofhint 'pick' expects at least one argument");
        }
        
        // TODO null checks
        hideLeftRule = env.getRule("hide_left");
        hideRightRule = env.getRule("hide_right");
    }

    /**
     * {@inheritDoc}
     * 
     * TODO DOC
     */
    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {

        try {
            // First call. Make the list of nodes to hide.
            if(node == reasonNode) {
                prepareRemoveList(node);
            } 

            if(!toRemoveList.isEmpty()) {
                // take last so no renumbering needed
                TermSelector toRemove = toRemoveList.removeLast();
                return makeRuleApp(toRemove, node);
            }

            return null;
        } catch (Exception e) {
            throw new StrategyException("The pick hint rule application failed", e);
        }
    }

    private RuleApplication makeRuleApp(TermSelector toRemove, ProofNode node) throws ProofException {
        
        Rule rule = toRemove.isAntecedent() ? hideLeftRule : hideRightRule;
        
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setRule(rule);
        ram.setProofNode(node);
        ram.setFindSelector(toRemove);
        ram.matchInstantiations();
        return ram;
    }

    private void prepareRemoveList(ProofNode node) throws FormatException {
        assert toRemoveList.isEmpty();

        makeAll(node, toRemoveList);

        assert "pick".equals(arguments[0]);
        for (int i = 1; i < arguments.length; i++) {
            toRemoveList.remove(new TermSelector(arguments[i]));
        }
    }

    private void makeAll(ProofNode node, List<TermSelector> list) {
        Sequent seq = node.getSequent();
        
        int anteSize = seq.getAntecedent().size();
        for (int i = 0; i < anteSize; i++) {
            list.add(new TermSelector(TermSelector.ANTECEDENT, i));
        }
        
        int succSize = seq.getSuccedent().size();
        for (int i = 0; i < succSize; i++) {
            list.add(new TermSelector(TermSelector.SUCCEDENT, i));
        }
    }

    
}