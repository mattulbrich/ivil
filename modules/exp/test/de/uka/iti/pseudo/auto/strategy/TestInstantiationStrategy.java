package de.uka.iti.pseudo.auto.strategy;

import java.io.InputStreamReader;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Sequent;

public class TestInstantiationStrategy extends TestCaseWithEnv {

    private Map<String, ProofObligation> problems;

    @Override
    protected void setUp() throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new InputStreamReader(getClass().getResourceAsStream("instTest.p")), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        env = em.getEnvironment();
        problems = em.getProofObligations();
    }


    public void testExEqInst1() throws Exception {
        Proof proof = problems.get("lemma:exEqInst1").initProof();
        InstantiationStrategy strategy = new InstantiationStrategy();
        StrategyManager sm = new StrategyManager(proof, env);
        strategy.init(proof, env, sm);
        strategy.beginSearch();
        RuleApplication ruleApp = strategy.findRuleApplication();
        assertNotNull(ruleApp);
        assertEquals("S.0", ruleApp.getFindSelector().toString());
        assertEquals(3, ruleApp.getSchemaVariableMapping().size());
        assertEquals(makeTerm("0"), ruleApp.getSchemaVariableMapping().get("%inst"));
        assertEquals(makeTerm("\\var x = 0"), ruleApp.getSchemaVariableMapping().get("%b"));
        assertEquals(makeTerm("\\var x as int"), ruleApp.getSchemaVariableMapping().get("%x"));

        proof.apply(ruleApp);
        Sequent seq2 = proof.getGoalByNumber(2).getSequent();
        assertEquals(makeTerm("0=0"), seq2.getSuccedent().get(1));
    }

    public void testExEqInst2() throws Exception {
        Proof proof = problems.get("lemma:exEqInst2").initProof();
        InstantiationStrategy strategy = new InstantiationStrategy();
        StrategyManager sm = new StrategyManager(proof, env);
        strategy.init(proof, env, sm);
        strategy.beginSearch();
        RuleApplication ruleApp = strategy.findRuleApplication();
        assertNotNull(ruleApp);
        assertEquals("S.0", ruleApp.getFindSelector().toString());
        assertEquals(3, ruleApp.getSchemaVariableMapping().size());
        assertEquals(makeTerm("0"), ruleApp.getSchemaVariableMapping().get("%inst"));
        assertEquals(makeTerm("(true & \\var x = 0) & true"), ruleApp.getSchemaVariableMapping().get("%b"));
        assertEquals(makeTerm("\\var x as int"), ruleApp.getSchemaVariableMapping().get("%x"));

        proof.apply(ruleApp);
        assertEquals(makeTerm("(true & 0=0) & true"), proof.getGoalByNumber(2).getSequent().getSuccedent().get(1));
    }

}
