package de.uka.iti.pseudo.auto;

import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;

public interface Strategy {

    public abstract RuleApplication findRuleApplication(Proof proof);

}