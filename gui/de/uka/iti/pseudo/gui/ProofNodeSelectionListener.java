package de.uka.iti.pseudo.gui;

import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

//TODO DOC
public interface ProofNodeSelectionListener {

    public void proofNodeSelected(ProofNode node);
    
    public void ruleApplicationSelected(RuleApplication ruleApplication);

}
