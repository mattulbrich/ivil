package de.uka.iti.pseudo.environment;

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Dump;
import de.uka.iti.pseudo.util.Util;

// It must be possible to prune a proof and replay it and obtain the same
// nodes.
public class TestProofReplay extends TestCase {

    private Environment env;
    private Sequent problem;

    private class Tree {
        RuleApplication ra;
        Tree[] children;

        public void checkEq(Tree t2) {
            if(t2.ra.getRule() != ra.getRule()
                    || !Util.equalOrNull(t2.ra.getFindSelector(), ra.getFindSelector())
                    || !Util.equalOrNull(t2.ra.getAssumeSelectors(), ra.getAssumeSelectors())
                    || !Util.equalOrNull(t2.ra.getSchemaUpdateMapping().toString(), ra.getSchemaUpdateMapping().toString())
                    || !Util.equalOrNull(t2.ra.getSchemaVariableMapping().toString(), ra.getSchemaVariableMapping().toString())
                    || !Util.equalOrNull(t2.ra.getTypeVariableMapping().toString(), ra.getTypeVariableMapping().toString())
                    || t2.children.length != children.length) {
                Dump.dumpRuleApplication(ra);
                Dump.dumpRuleApplication(t2.ra);
                fail("Not same proof!");
            }

            for (int i = 0; i < children.length; i++) {
                children[i].checkEq(t2.children[i]);
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        Parser parser = new Parser();
        URL url = getClass().getResource("replay_test.p");
        EnvironmentMaker em = new EnvironmentMaker(parser, url);
        env = em.getEnvironment();

        problem = em.getProblemSequents().values().iterator().next();
    }

    public void testProofReplay() throws Exception {
        Proof proof = new Proof(problem, env);
        runProof(proof);
        assertFalse(proof.hasOpenGoals());

        Tree result1 = dump(proof.getRoot());
        proof.prune(proof.getRoot());

        runProof(proof);
        assertFalse(proof.hasOpenGoals());

        Tree result2 = dump(proof.getRoot());

        result1.checkEq(result2);
    }


    private Tree dump(ProofNode node) {
        Tree result = new Tree();
        result.ra = node.getAppliedRuleApp();
        List<ProofNode> children = node.getChildren();
        result.children = new Tree[children.size()];
        for (int i = 0; i < result.children.length; i++) {
            result.children[i] = dump(children.get(i));
        }
        return result;
    }

    public void runProof(Proof proof) throws Exception {
        StrategyManager strategyManager = new StrategyManager(proof, env);
        strategyManager.registerAllKnownStrategies();
        Strategy strategy = strategyManager.getSelectedStrategy();

        strategy.beginSearch();

        for (int count = 0; count < 10000; count++) {

            RuleApplication ruleApp;
            ruleApp = strategy.findRuleApplication();

            if (ruleApp == null) {
                break;
            }

            System.out.println(count + " " + ruleApp.getRule());

            proof.apply(ruleApp);
            strategy.notifyRuleApplication(ruleApp);
        }

        List<ProofNode> openGoals = proof.getOpenGoals();

        if (openGoals.isEmpty()) {
            return;
        }
    }

}
