package de.uka.iti.pseudo.parser.file;

import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.auto.script.ProofScript.Kind;
import de.uka.iti.pseudo.auto.script.ProofScript.Obligation;
import de.uka.iti.pseudo.auto.script.ProofScriptCommand;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.environment.creation.ProofScriptExtractor;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.util.Dump;

public class TestProofScripts extends TestCaseWithEnv {

    public static class MockProofScriptCommand implements ProofScriptCommand {
        @Override
        public String getName() { return "mock"; }
    }

    private Environment testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" \n" +
                "plugins proofScriptCommand \"de.uka.iti.pseudo.parser.file." +
                "TestProofScripts$MockProofScriptCommand\"\nproblem p1 : true\n" +
                string), "*test*");
//        de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
        // env.dump();
        return env;
    }

    private void testEnvFail(String string) {
        try {
            Environment env = testEnv(string);
            Dump.dumpEnv(env);
            fail("Should have failed");
        } catch (Exception e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testParse() throws Exception {
        Parser fp = new Parser();
        URL url = getClass().getResource("scripttest.p");
        if(url == null) {
            throw new Exception("ruletest.p not found");
        }
        EnvironmentMaker em = new EnvironmentMaker(fp, url);
        Map<Obligation, ProofScript> scripts = em.getProofScripts();

        if(true) {
            Dump.dumpEnv(em.getEnvironment());
            for (ProofScript ps : scripts.values()) {
                Dump.dumpProofScript(ps);
            }
        }

        assertEquals(6, scripts.size());
        assertTrue(scripts.containsKey(new Obligation(Kind.RULE, "r1")));
        assertTrue(scripts.containsKey(new Obligation(Kind.RULE, "r2")));
        assertTrue(scripts.containsKey(new Obligation(Kind.PROBLEM, "p1")));
        assertTrue(scripts.containsKey(new Obligation(Kind.PROBLEM, "p2")));
        assertTrue(scripts.containsKey(new Obligation(Kind.PROGRAM, "Q1")));
        assertTrue(scripts.containsKey(new Obligation(Kind.PROGRAM, "Q2")));

        // the proof for p1 has annotations which are special.
        ProofScriptNode n = scripts.get(new Obligation(Kind.PROBLEM, "p1")).getRoot();
        checkPrefix(n, "");
    }

    private void checkPrefix(ProofScriptNode n, String string) {
        assertEquals(string, n.getArgument("#1"));
        List<ProofScriptNode> children = n.getChildren();
        for (int i = 0; i < children.size(); i++) {
            checkPrefix(children.get(i), string + i);
        }
    }

    // to be implemented ...
    public void testSameObligationTwice() throws Exception {
        testEnvFail("proof problem p1 (mock) proof problem p1 (mock)");
    }

    public void testDeclarations() throws Exception {
        testEnv("problem p2:true proof (mock)");
        testEnvFail("function int f proof (mock)");
        testEnvFail("sort S proof (mock)");
        testEnvFail("problem true proof (mock)");
    }

    public void testTwoScripts() {
        testEnvFail("proof source \"source.p\" proof source \"source2.p\"");
        testEnvFail("properties proof.sourcefile \"source.p\" proof source \"source2.p\"");
    }

    public void testScriptProperty() throws Exception {
        env = testEnv("proof source \"/path/sources.p\"");
        assertEquals("/path/sources.p",
                env.getProperty(ProofScriptExtractor.PROOF_SOURCE_PROPERTY));
    }

}
