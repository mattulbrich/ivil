package de.uka.iti.pseudo.auto.strategy;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.TermUtil;

// This does not seem to be hinished
public class InstantiationStrategy extends AbstractStrategy {

    private static final String INSTANTIATION_MARKER = "ivil.instantiation";

    private Environment env;
    private Rule forallLeft;
    private Rule existsRight;

    @Override
    public void init(Proof proof, Environment env, StrategyManager strategyManager)
            throws StrategyException {
        super.init(proof, env, strategyManager);
        this.env = env;

        this.forallLeft = env.getRule("forall_left");
        if(forallLeft == null) {
            throw new StrategyException("Rule 'forall_left' must be defined " +
                    "for instantiation strategy");
        }

        this.existsRight = env.getRule("exists_right");
        if(existsRight == null) {
            throw new StrategyException("Rule 'exists_right' must be defined " +
                    "for instantiation strategy");
        }
    }

    @Override
    public RuleApplication findRuleApplication(ProofNode node) throws StrategyException {

        Sequent sequent = node.getSequent();

        int pos = 0;
        for (Term formula : sequent.getAntecedent()) {
            if(TermUtil.isForall(formula)) {
                Term inst = findLeftInstantiation(formula, node);
                if(inst != null) {
                    return makeRuleApplication(new TermSelector(TermSelector.ANTECEDENT, pos),
                            inst, node);
                }
            }
            pos ++;
        }

        pos = 0;
        for (Term formula : sequent.getSuccedent()) {
            if(TermUtil.isExists(formula)) {
                Term inst = findRightInstantiation(formula, node);
                if(inst != null) {
                    return makeRuleApplication(new TermSelector(TermSelector.SUCCEDENT, pos),
                            inst, node);
                }
            }
            pos ++;
        }

        return null;
    }

    private RuleApplication makeRuleApplication(TermSelector termSelector, Term inst, ProofNode node)
            throws StrategyException {
        try {
            RuleApplicationMaker result = new RuleApplicationMaker(env);
            result.setProofNode(node);
            result.setFindSelector(termSelector);
            result.getTermMatcher().addInstantiation("%inst", inst);

            if(termSelector.isAntecedent()) {
                result.setRule(forallLeft);
            } else {
                result.setRule(existsRight);
            }

            result.getProperties().put(INSTANTIATION_MARKER,
                    makeInstantiationMarker(termSelector.selectTopterm(node.getSequent()), inst));

            result.matchInstantiations();
            return result;
        } catch (Exception e) {
            Log.log(Log.ERROR,
                    "Instantiation strategy cannot create rule: node=%d, inst=%s, selector=%d",
                    node.getNumber(), inst, termSelector);
            throw new StrategyException("Cannot create rule application for " + inst, e);
        }
    }

    private String makeInstantiationMarker(Term formula, Term inst) {
        return inst  + " INTO " + formula;
    }

    private Term findLeftInstantiation(Term formula, ProofNode node) {
        // TODO implement all_left implementation
        return null;
    }

    private Term findRightInstantiation(Term formula, ProofNode node) {
        Term result = findExistentialEqualityMatch(formula, node);
        if(result != null) {
            return result;
        }

        // TODO implement ex_right implementation
        return null;
    }

    private Term findExistentialEqualityMatch(Term formula, ProofNode node) {
        assert TermUtil.isExists(formula) : "This must be an existential quant";
        Binding quant = (Binding)formula;

        assert quant.getVariable() instanceof Variable;
        Variable boundVar = (Variable) quant.getVariable();

        return findExEqualityMatch0(formula, node, boundVar, quant.getSubterm(0));
    }

    private Term findExEqualityMatch0(Term formula, ProofNode node, Variable var, Term term) {
        if (TermUtil.isEquality(term)) {

            // TODO make this with matching
            if(var.equals(term.getSubterm(0))) {
                Term candidate = term.getSubterm(1);
                if(checkNotYetInstantiated(formula, candidate, node)) {
                    return candidate;
                }
            }

            if(var.equals(term.getSubterm(1))) {
                Term candidate = term.getSubterm(0);
                if(checkNotYetInstantiated(formula, candidate, node)) {
                    return candidate;
                }
            }
        } else

        if (TermUtil.isConjunction(term)) {
            Term result = findExEqualityMatch0(formula, node, var, term.getSubterm(0));
            if (result == null) {
                result = findExEqualityMatch0(formula, node, var, term.getSubterm(1));
            }
            return result;
        }

        return null;
    }

    private boolean checkNotYetInstantiated(Term formula, Term candidate, ProofNode node) {
        String marker = makeInstantiationMarker(formula, candidate);
        ProofNode n = node.getParent();
        while(n != null) {
            if(marker.equals(n.getAppliedRuleApp().getProperties().get(INSTANTIATION_MARKER))) {
                return false;
            }
            n = n.getParent();
        }
        return true;
    }

    @Override
    public String toString() {
        return "Instantiation Strategy";
    }

}
