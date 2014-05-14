package de.uka.iti.pseudo.gui.extensions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.util.Log;

public class PropositionalExpansionExt implements ContextExtension {

    private final static String[] NON_SPLIT_RULES =
        { "and_left", "or_right", "impl_right" };

    private final static String[] SPLIT_RULES =
        { "and_right"};// "or_left", "impl_left" };

    @Override public String getName() {
        return "Propositional Expansion";
    }

    @Override public String getDescription() {
        return "tbd";
    }

    @Override public boolean shouldOffer(ProofCenter proofCenter) {
        return true;
    }

    @Override public void run(ProofCenter proofCenter) {
        apply(proofCenter, NON_SPLIT_RULES);
        apply(proofCenter, SPLIT_RULES);
    }

    private void apply(ProofCenter proofCenter, String[] ruleset) {
        LinkedList<ProofNode> openGoals = new LinkedList<ProofNode>();
        collectOpenGoals(proofCenter.getCurrentProofNode(), openGoals);

        while(!openGoals.isEmpty()) {
            Environment env = proofCenter.getEnvironment();

            ProofNode n = openGoals.remove(0);
            rules: for (String rulename : ruleset) {
                Rule rule = env.getRule(rulename);
                if(rule != null) {
                    RuleApplicationMaker ram = new RuleApplicationMaker(env);
                    ram.setProofNode(n);
                    ram.setRule(rule);
                    int size = n.getSequent().getAntecedent().size();
                    for (int i = 0; i < size; i++) {
                        ram.setFindSelector(new TermSelector(TermSelector.ANTECEDENT, i));
                        try {
                            ram.matchInstantiations();
                            proofCenter.getProof().apply(ram, env);
                            openGoals.addAll(n.getChildren());
                            continue rules;
                        } catch (ProofException e) {
                            // did not match, so what ...
                        }
                    }

                    size = n.getSequent().getSuccedent().size();
                    for (int i = 0; i < size; i++) {
                        ram.setFindSelector(new TermSelector(TermSelector.SUCCEDENT, i));
                        try {
                            ram.matchInstantiations();
                            proofCenter.getProof().apply(ram, env);
                            openGoals.addAll(n.getChildren());
                            continue rules;
                        } catch (ProofException e) {
                            // did not match, so what ...
                        }
                    }

                } else {
                    Log.log(Log.WARNING, "Unknown rule %s", rulename);
                }
            }
        }
    }

    private void collectOpenGoals(ProofNode pn, Collection<ProofNode> list) {
        List<ProofNode> children = pn.getChildren();
        if(children == null) {
            list.add(pn);
        } else {
            for (ProofNode child : children) {
                collectOpenGoals(child, list);
            }
        }
    }

}
