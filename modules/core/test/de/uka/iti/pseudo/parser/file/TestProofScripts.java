package de.uka.iti.pseudo.parser.file;

import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.auto.script.ProofScriptCommand;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.Dump;
import de.uka.iti.pseudo.util.Triple;

public class TestProofScripts extends TestCaseWithEnv {

    public static class MockProofScriptCommand implements ProofScriptCommand {
        @Override
        public String getName() { return "mock"; }

        @Override
        public void checkSyntax(ProofScriptNode node) throws StrategyException {
            // no checks
        }

        @Override
        public List<ProofNode> apply(ProofScriptNode node, ProofNode proofNode)
                throws StrategyException {
            // we do nothing
            return null;
        }
    }

    private Map<String, ProofObligation> proofObligations;

    private void testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" \n" +
                "plugins proofScriptCommand \"de.uka.iti.pseudo.parser.file." +
                "TestProofScripts$MockProofScriptCommand\"\nlemma p1 true\n" +
                string), "*test*");
//        de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        env = em.getEnvironment();
        proofObligations = em.getProofObligations();
    }

    private void testEnvFail(String string) {
        try {
            testEnv(string);
            Dump.dumpEnv(env);
            fail("Should have failed");
        } catch (Exception e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    // revealed a bug
    public void testParse() throws Exception {
        Parser fp = new Parser();
        URL url = getClass().getResource("scripttest.p");
        if(url == null) {
            throw new Exception("scripttest.p not found");
        }
        EnvironmentMaker em = new EnvironmentMaker(fp, url);
        Map<String, ProofObligation> obligations = em.getProofObligations();

        if(VERBOSE) {
            Dump.dumpEnv(em.getEnvironment());
        }

        assertEquals(9, obligations.size());
        assertTrue(obligations.containsKey(ProofObligation.RulePO.PREFIX + "r1"));
        assertNotNull(obligations.get(ProofObligation.RulePO.PREFIX + "r1").getProofScript());
        assertTrue(obligations.containsKey(ProofObligation.RulePO.PREFIX + "r2"));
        assertNotNull(obligations.get(ProofObligation.RulePO.PREFIX + "r2").getProofScript());

        assertTrue(obligations.containsKey(ProofObligation.LemmaPO.PREFIX + "p1"));
        assertNotNull(obligations.get(ProofObligation.LemmaPO.PREFIX + "p1").getProofScript());
        assertTrue(obligations.containsKey(ProofObligation.LemmaPO.PREFIX + "p2"));
        assertNotNull(obligations.get(ProofObligation.LemmaPO.PREFIX + "p2").getProofScript());

        assertTrue(obligations.containsKey(ProofObligation.ProgramPO.PREFIX + "Q1" +
                ProofObligation.ProgramPO.SUFFIX_TOTAL));
        assertNotNull(obligations.get(ProofObligation.ProgramPO.PREFIX + "Q1" +
                ProofObligation.ProgramPO.SUFFIX_TOTAL).getProofScript());
        assertTrue(obligations.containsKey(ProofObligation.ProgramPO.PREFIX + "Q2" +
                ProofObligation.ProgramPO.SUFFIX_TOTAL));
        assertNotNull(obligations.get(ProofObligation.ProgramPO.PREFIX + "Q2"+
                ProofObligation.ProgramPO.SUFFIX_TOTAL).getProofScript());

        assertTrue(obligations.containsKey(ProofObligation.ProgramPO.PREFIX + "Q1" +
                ProofObligation.ProgramPO.SUFFIX_PARTIAL));
        assertNull(obligations.get(ProofObligation.ProgramPO.PREFIX + "Q1" +
                ProofObligation.ProgramPO.SUFFIX_PARTIAL).getProofScript());
        assertTrue(obligations.containsKey(ProofObligation.ProgramPO.PREFIX + "Q2" +
                ProofObligation.ProgramPO.SUFFIX_PARTIAL));
        assertNull(obligations.get(ProofObligation.ProgramPO.PREFIX + "Q2"+
                ProofObligation.ProgramPO.SUFFIX_PARTIAL).getProofScript());

        String key = ProofObligation.RulePO.PREFIX + "known_rule_proved_outside";
        assertTrue(obligations.containsKey(key));
        assertNotNull(obligations.get(key).getProofScript());

        env = em.getEnvironment();
        assertEquals("scripttest.proof", env.getProperty(ProofScript.PROOF_SOURCE_PROPERTY));
    }

    public void testTree() throws Exception {

        Parser fp = new Parser();
        URL url = getClass().getResource("scripttest.p");
        if(url == null) {
            throw new Exception("scripttest.p not found");
        }
        EnvironmentMaker em = new EnvironmentMaker(fp, url);
        Map<String, ProofObligation> obligations = em.getProofObligations();

        if(VERBOSE) {
            Dump.dumpEnv(em.getEnvironment());
        }

        // the proof for p1 has annotations which are special.
        ProofObligation po1 = obligations.get(ProofObligation.LemmaPO.PREFIX + "p1");
        ProofScript script1 = po1.getProofScript();
        ProofScriptNode n = script1.getRoot();
        checkPrefix(n, "");

        assertEquals("", n.getArgument("#1"));
        assertEquals("c", n.getArgument("#2"));
        assertEquals("a", n.getArgument("a"));
        assertEquals("b", n.getArgument("b"));
        assertEquals("d", n.getArgument("#3"));

    }

    private void checkPrefix(ProofScriptNode n, String string) {
        assertEquals(string, n.getArgument("#1"));
        List<ProofScriptNode> children = n.getChildren();
        for (int i = 0; i < children.size(); i++) {
            checkPrefix(children.get(i), string + i);
        }
    }

    public void testSameObligationTwice() throws Exception {
        testEnvFail("proof problem p1 (mock) proof problem p1 (mock)");
    }

    public void testEmptyProofStep() throws Exception {
        testEnv("lemma p2 true proof (mock; mock () ())");
        testEnv("lemma p2 true proof ()");
    }

    public void testDeclarations() throws Exception {
        testEnv("lemma p2 true proof (mock)");
        testEnv("rule r2 add |- true proof (mock)");
        testEnvFail("function int f proof (mock)");
        testEnvFail("sort S proof (mock)");
    }

    public void testTwoScripts() {
        testEnvFail("proof source \"source.p\" proof source \"source2.p\"");
        testEnvFail("properties proof.sourcefile \"source.p\" proof source \"source2.p\"");
    }

    public void testProofScriptPO() throws Exception {
        Triple<Environment, Map<String, ProofObligation>, Map<String, ProofScript>> parsed =
                makeEnvAndProofObls("plugins proofScriptCommand \"de.uka.iti.pseudo.parser.file." +
                        "TestProofScripts$MockProofScriptCommand\"\n"+
                        "lemma l1 true proof (mock)\n" +
                                "rule r1 add |- true proof (mock)\n" +
                                "program P1 goto 0\n" +
                        "proof program P1.step (mock)");

        Map<String, ProofObligation> proofObs = parsed.snd();

        assertNotNull(proofObs.get("lemma:l1"));
        assertNotNull(proofObs.get("lemma:l1").getProofScript());
        assertNotNull(proofObs.get("rule:r1"));
        assertNotNull(proofObs.get("rule:r1").getProofScript());

        Map<String, ProofScript> assocProofs = parsed.trd();

        assertEquals(1, assocProofs.size());
        assertNotNull(assocProofs.get("program:P1.step"));
    }

}
