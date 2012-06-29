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

public class InstantiationStrategy extends AbstractStrategy {

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
            result.getTermMatcher().addInstantiation(
                    SchemaVariable.getInst("%inst", Environment.getBoolType()), inst);

            if(termSelector.isAntecedent()) {
                result.setRule(forallLeft);
            } else {
                result.setRule(existsRight);
            }

            result.matchInstantiations();
            return result;
        } catch (Exception e) {
            Log.log(Log.ERROR,
                    "Instantiation strategy cannot create rule: node=%d, inst=%s, selector=%d",
                    node.getNumber(), inst, termSelector);
            throw new StrategyException("Cannot create rule application for " + inst, e);
        }
    }

    private Term findLeftInstantiation(Term formula, ProofNode node) {
        // TODO implement all_left implementation
        return null;
    }

    private Term findRightInstantiation(Term formula, ProofNode node) {
        Term result = findExistentialEqualityMatch(formula);
        if(result != null) {
            return result;
        }

        // TODO implement ex_right implementation
        return null;
    }

    private Term findExistentialEqualityMatch(Term formula) {
        assert TermUtil.isExists(formula) : "This must be an existential quant";
        Binding quant = (Binding)formula;

        assert quant.getVariable() instanceof Variable;
        Variable boundVar = (Variable) quant.getVariable();

        return findExEqualityMatch0(boundVar, quant.getSubterm(0));
    }

    private Term findExEqualityMatch0(Variable var, Term term) {
        if (TermUtil.isEquality(term)) {

            // TODO make this with matching
            if(var.equals(term.getSubterm(0))) {
                return term.getSubterm(1);
            } else if(var.equals(term.getSubterm(1))) {
                return term.getSubterm(0);
            }
        } else

        if (TermUtil.isConjunction(term)) {
            Term result = findExEqualityMatch0(var, term.getSubterm(0));
            if (result == null) {
                result = findExEqualityMatch0(var, term.getSubterm(1));
            }
            return result;
        }

        return null;
    }

}
