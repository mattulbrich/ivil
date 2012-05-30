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

import java.util.List;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

/**
 * The Class CutProofHint implements a proof hint which applies the cut rule.
 * 
 * @ivildoc "Proof hint/cut"
 * 
 * <h2>Proof hint <code>cut</code></h2>
 * 
 * In order to perform a case distinction on a branch, this hint can be used.
 * 
 * <h3>Arguments</h3>
 * cut takes one argument which is the formula upon which to perform the cut.
 * 
 * <h3>Example</h3>
 * <pre>
 * assert x*x >= 0 ; "examine by sign §(cut 'x >= 0')"
 * </pre>
 */
public class CutProofHint implements ProofHint {

    @Override
    public String getKey() {
        return "cut";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new CutHintAppFinder(env, arguments);
    }
}

/**
 * This implementation applies the cut rule.
 */
class CutHintAppFinder extends HintRuleAppFinder {

    private final Environment env;

    public CutHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;
        
        if(arguments.size() != 2) {
            throw new StrategyException("The proofhint 'cut' expects exactly one argument");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>The cut rule is applied only on the reasonNode. 
     */
    @Override
    public RuleApplication findRuleApplication(ProofNode node,
            ProofNode reasonNode) throws StrategyException {

        // Only applicable to the reasonNode, not later
        if(node != reasonNode) {
            return null;
        }

        // Make the cut formula
        try {
            Term formula = TermMaker.makeAndTypeTerm(arguments[1], env);
            RuleApplicationMaker ram = new RuleApplicationMaker(env);
            ram.setRule(env.getRule("cut"));
            ram.getTermMatcher().addInstantiation(SchemaVariable.getInst("%inst", Environment.getBoolType()), formula);
            ram.setProofNode(node);
            return ram;
        } catch (Exception e) {
            throw new StrategyException("Cannot create cut formula from " + arguments[1], e);
        }
    }
    
}