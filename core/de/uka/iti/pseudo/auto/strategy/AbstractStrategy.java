/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

/**
 * This abstract class keeps a list of proof nodes to which this strategy cannot
 * be applied.
 * 
 * It implements the findRuleApplication by querying only those proof nodes
 * which are not tagged as "not matching" using
 * 
 */
public abstract class AbstractStrategy implements Strategy {
    
    private Set<ProofNode> notMatching = new HashSet<ProofNode>();
    
    private Proof proof;
    
    @Override
    public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {
        this.proof = proof;
    }

    @Override
    public void beginSearch() throws StrategyException {
    }

    @Override
    public void endSearch() {
        notMatching.clear();
    }

    @Override
    public RuleApplication findRuleApplication()
            throws StrategyException {
        List<ProofNode> openGoals = proof.getOpenGoals();
        
        int index = 0;
        for (ProofNode goal : openGoals) {
            if (!notMatching.contains(goal)) {
                RuleApplication ra = findRuleApplication(index);
                if (ra != null) {
                    return ra;
                } else {
                    notMatching.add(goal);
                }
            }
            index ++;
        }
        
        return null;
    }

    protected abstract RuleApplication findRuleApplication(int goalIndex) throws StrategyException;

    @Override
    public void notifyRuleApplication(RuleApplication ruleApp)
            throws StrategyException {
    }

    /**
     * @return the proof
     */
    public Proof getProof() {
        return proof;
    }

}
