package de.uka.iti.pseudo.auto.strategy;

import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;

public interface Strategy {

    RuleApplication findRuleApplication(Proof proof) throws StrategyException;

}