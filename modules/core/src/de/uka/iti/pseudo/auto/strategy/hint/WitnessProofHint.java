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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.creation.TermMaker;

/**
 * The Class WitnessProofHint implements a proof hint which instantiates
 * quantifiers under special circumstances.
 *
 * Is a special case of inst
 *
 * @ ivildoc "Proof hint/inst"
 *
 * <h2>Proof hint <code>inst</code></h2>
 *
 * This hint can be used to instantiate a universal quantifier in the antecedent
 * or an existential one in the succedent.
 *
 * <h3>Arguments</h3>
 *
 * <tt>hint</tt> takes three arguments:
 * <ol>
 * <li> The first argument is either the quantified formula to be instantiated or
 * the variable name to be instantiated.
 * <li> The second argument must always be "with"
 * <li> The third argument is the term with which the quantified variable is to
 * be instantiated.
 * <li> An optional fourth argument 'hide' may be added to apply the hiding rule
 * </ol>
 *
 * <h3>Example</h3>
 * Assuming that <code> (\forall x; p(x) = x+2)</code> is in the antecedent
 * <pre>
 * assert p(0) = 2 ; "use the quantifier by §(inst x with 0)"
 * assert !p(1) = 2 ; "use the quantifier by §(inst '(\forall x; p(x)=x+2)' with 0)"
 * </pre>
 */
public class WitnessProofHint implements ProofHint {

    @Override
    public String getName() {
        return "witness";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new WitnessHintAppFinder(env, arguments);
    }
}

class WitnessHintAppFinder extends HintRuleAppFinder {

    private final Term instSV;
    private final Set<ProofNode> appliedProofNodes = new HashSet<ProofNode>();
    private final String witness;
    private final Environment env;
    private final Binder forallBinder;
    private final Binder existsBinder;
    private final Rule forallRule;
    private final Rule existsRule;
    private final boolean hides;
    private final Rule updSimpRule;

    public WitnessHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;

        if(arguments.size() < 2 || arguments.size() > 3) {
            throw new StrategyException("The proofhint 'witness' expects 1 argument (plus perhas 'hide'");
        }

        assert "witness".equals(arguments.get(0));

        if(arguments.size() == 3) {
            if(!"hide".equals(arguments.get(2))) {
                throw new StrategyException("The optional last argument must be 'hide' if specified");
            }
            this.hides = true;
        } else {
            this.hides = false;
        }

        this.witness = arguments.get(1);
        this.forallBinder = env.getBinder("\\forall");
        this.existsBinder = env.getBinder("\\exists");

        if(forallBinder == null || existsBinder == null) {
            throw new StrategyException("The quantifiers cannot be loaded");
        }

        if(hides) {
            this.forallRule = env.getRule("forall_left_hide");
            this.existsRule = env.getRule("exists_right_hide");
        } else {
            this.forallRule = env.getRule("forall_left");
            this.existsRule = env.getRule("exists_right");
        }
        this.updSimpRule = env.getRule("deep_update_simplification");
        if(forallRule == null || existsRule == null) {
            throw new StrategyException("The quantifier rules cannot be loaded");
        }

        try {
            this.instSV = SchemaVariable.getInst("%inst", SchemaType.getInst("%'inst"));
        } catch (TermException e) {
            throw new StrategyException(e);
        }
    }

    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {

        if(alreadyAppliedOnBranch(node, reasonNode)) {
            return null;
        }

        // TODO also in the antecedent.

        try {
            List<Term> succedent = node.getSequent().getSuccedent();
            Term target = succedent.get(0);
            if(target instanceof UpdateTerm) {
                RuleApplicationMaker ram = new RuleApplicationMaker(env);
                ram.setProofNode(node);
                ram.setFindSelector(new TermSelector(TermSelector.SUCCEDENT, 0));
                ram.setRule(updSimpRule);
                ram.matchInstantiations();
                return ram;
            } else {
                Term inst = TermMaker.makeAndTypeTerm(witness, env, node.getLocalSymbolTable());
                RuleApplicationMaker ram = new RuleApplicationMaker(env);
                ram.setProofNode(node);
                ram.setFindSelector(new TermSelector(TermSelector.SUCCEDENT, 0));
                ram.setRule(existsRule);
                ram.getTermMatcher().leftMatch(instSV, inst);
                ram.matchInstantiations();
                appliedProofNodes.add(node);
                return ram;
            }

        } catch (Exception e) {
            throw new StrategyException("Instantiation proof hint failed", e);
        }
    }

    private boolean alreadyAppliedOnBranch(ProofNode node, ProofNode reasonNode) {
        while(node != reasonNode) {
            if(appliedProofNodes.contains(node)) {
                return true;
            }
            node = node.getParent();
            assert node != null : "The node is not a child of reasonNode!";
        }
        if(appliedProofNodes.contains(reasonNode)) {
            return true;
        }
        return false;
    }
}