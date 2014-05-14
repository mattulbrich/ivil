package de.uka.iti.pseudo.gui.extensions;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;

public class RefinementExpansionExt implements ContextExtension {

    @Override
    public String getName() {
        return "Refinement";
    }

    @Override
    public String getDescription() {
        return "Apply the refinement rule and split the proof obligations (EXP!)";
    }

    @Override
    public boolean shouldOffer(ProofCenter proofCenter) {
        return !proofCenter.getCurrentProofNode().isClosed();
    }

    @Override
    public void run(ProofCenter proofCenter) throws ProofException {

        Proof proof = proofCenter.getProof();
        ProofNode goal = proofCenter.getCurrentProofNode();

        Environment env = proofCenter.getEnvironment();
        RuleApplicationMaker ram = new RuleApplicationMaker(env);
        ram.setRule(env.getRule("refinement"));
        ram.setFindSelector(new TermSelector(TermSelector.SUCCEDENT, 0));
        ram.setProofNode(goal);
        ram.matchInstantiations();

        proof.apply(ram, env);
        goal = goal.getChildren().get(0);
        expandAndRight(goal, env, proof);
    }

    private void expandAndRight(ProofNode goal, Environment env, Proof proof) throws ProofException {

        // TODO IndexOutOf
        Term s0 = goal.getSequent().getSuccedent().get(0);
        if(isConj(s0)) {
            RuleApplicationMaker ram = new RuleApplicationMaker(env);
            ram.setRule(env.getRule("and_right"));
            ram.setFindSelector(new TermSelector(TermSelector.SUCCEDENT, 0));
            ram.setProofNode(goal);
            ram.matchInstantiations();
            proof.apply(ram, env);

            List<ProofNode> children = goal.getChildren();
            expandAndRight(children.get(0), env, proof);
            expandAndRight(children.get(1), env, proof);
        }
    }

    private boolean isConj(Term t) {
        return t instanceof Application && ((Application)t).getFunction().getName().equals("$and");
    }

}
