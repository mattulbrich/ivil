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
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermMaker;

/**
 * The Class InstantiateProofHint implements a proof hint which instantiates
 * quantifiers.
 *
 * @ivildoc "Proof hint/inst"
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
 * </ol>
 *
 * <h3>Example</h3>
 * Assuming that <code> (\forall x; p(x) = x+2)</code> is in the antecedent
 * <pre>
 * assert p(0) = 2 ; "use the quantifier by §(inst x with 0)"
 * assert !p(1) = 2 ; "use the quantifier by §(inst '(\forall x; p(x)=x+2)' with 0)"
 * </pre>
 */
public class InstantiateProofHint implements ProofHint {

    @Override
    public String getKey() {
        return "inst";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new InstantiationHintAppFinder(env, arguments);
    }
}

class InstantiationHintAppFinder extends HintRuleAppFinder {

    private final Term instSV;
    private final Set<ProofNode> appliedProofNodes = new HashSet<ProofNode>();
    private final String replaced;
    private final String replacement;
    private final Environment env;
    private final Binder forallBinder;
    private final Binder existsBinder;
    private final Rule forallRule;
    private final Rule existsRule;

    public InstantiationHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;

        if(arguments.size() != 4) {
            throw new StrategyException("The proofhint 'inst' expects two argument");
        }

        assert "inst".equals(arguments.get(0));

        if(!"with".equals(arguments.get(2))) {
            throw new StrategyException("The first and second argument must be separated by 'with'");
        }

        this.replaced = arguments.get(1);
        this.replacement = arguments.get(3);

        this.forallBinder = env.getBinder("\\forall");
        this.existsBinder = env.getBinder("\\forall");
        if(forallBinder == null || existsBinder == null) {
            throw new StrategyException("The quantifiers cannot be loaded");
        }

        this.forallRule = env.getRule("forall_left");
        this.existsRule = env.getRule("exists_right");
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

        RuleApplication result;

        if(alreadyAppliedOnBranch(node, reasonNode)) {
            return null;
        }

        try {
            // if the first character is '(' then a formula to be instantiated
            // has been given, otherwise this would be a variable.
            if(replaced.startsWith("(")) {
                result = null ; //instantiateFormula(node, reasonNode);
            } else {
                result = instantiateVariable(node, reasonNode);
            }
        } catch (Exception e) {
            throw new StrategyException("Instantiation proof hint failed", e);
        }

        appliedProofNodes.add(result.getProofNode());
        return result;
    }

    private RuleApplication instantiateVariable(ProofNode node, ProofNode reasonNode)
            throws ParseException, ASTVisitException, TermException, ProofException {

        Term inst = TermMaker.makeAndTypeTerm(replacement, env);
        Sequent sequent = node.getSequent();

        int termno = 0;
        for (Term term : sequent.getAntecedent()) {
            if (term instanceof Binding) {
                Binding binding = (Binding) term;
                Binder binder = binding.getBinder();
                if(binder == forallBinder &&
                        binding.getVariable().getName().equals(replaced)) {
                    RuleApplicationMaker ram = new RuleApplicationMaker(env);
                    ram.setProofNode(node);
                    ram.setFindSelector(new TermSelector(TermSelector.ANTECEDENT, termno));
                    ram.setRule(forallRule);
                    ram.getTermMatcher().leftMatch(instSV, inst);
                    ram.matchInstantiations();
                    return ram;
                }
            }
            termno ++;
        }

        termno = 0;
        for (Term term : sequent.getSuccedent()) {
            if (term instanceof Binding) {
                Binding binding = (Binding) term;
                Binder binder = binding.getBinder();
                if(binder == existsBinder &&
                        binding.getVariable().getName().equals(replaced)) {
                    RuleApplicationMaker ram = new RuleApplicationMaker(env);
                    ram.setProofNode(node);
                    ram.setFindSelector(new TermSelector(TermSelector.SUCCEDENT, termno));
                    ram.setRule(existsRule);
                    ram.getTermMatcher().leftMatch(instSV, inst);
                    ram.matchInstantiations();
                    return ram;
                }
            }
            termno ++;
        }

        return null;
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