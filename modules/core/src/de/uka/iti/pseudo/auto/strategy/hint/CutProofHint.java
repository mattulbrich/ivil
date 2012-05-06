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

public class CutProofHint implements ProofHint {

    @Override
    public Object getKey() {
        return "cut";
    }

    @Override
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException {
        return new CutHintAppFinder(env, arguments);
    }
}

class CutHintAppFinder extends HintRuleAppFinder {

    private final Environment env;

    public CutHintAppFinder(Environment env, List<String> arguments) throws StrategyException {
        super(arguments);
        this.env = env;
        
        if(arguments.size() != 2) {
            throw new StrategyException("The proofhint 'rule' expects exactly one argument");
        }
    }

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