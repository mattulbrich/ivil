package de.uka.iti.pseudo.parser.file;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Named;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;

public class TestProofObligations extends TestCaseWithEnv {

    private Map<String, ProofObligation> proofObligations;

    private void parseEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
//        de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        proofObligations = em.getProofObligations();
        env = em.getEnvironment();
    }

    private <E> Set<E> asSet(E... vals) {
        return new HashSet<E>(Arrays.asList(vals));
    }

    private Set<String> namesOf(Collection<? extends Named> collection) {
        Set<String> result = new HashSet<String>();
        for (Named named : collection) {
            result.add(named.getName());
        }
        return result;
    }

    public void testLemmas() throws Exception {
        parseEnv("lemma lemma1 true lemma lemma2 true");
        assertEquals(asSet("lemma:lemma1", "lemma:lemma2"), proofObligations.keySet());
        assertEquals(asSet("lemma1", "lemma2"), namesOf(env.getLocalLemmas()));
    }

    public void testAxioms() throws Exception {
        parseEnv("lemma lemma1 true axiom lemma2 true");
        assertEquals(asSet("lemma:lemma1"), proofObligations.keySet());
        assertEquals(asSet("lemma1", "lemma2"), namesOf(env.getLocalLemmas()));
    }

    public void testRules() throws Exception {
        parseEnv("rule rule1 find true replace true\n" +
                "rule rule2 find true replace false");
        assertEquals(asSet("rule:rule1", "rule:rule2"), proofObligations.keySet());
        assertEquals(asSet("rule1", "rule2"), namesOf(env.getLocalRules()));
    }

    public void testAxiomRules() throws Exception {
        parseEnv("rule rule1 find true replace true\n" +
                "axiom rule rule2 find true replace false");
        assertEquals(asSet("rule:rule1"), proofObligations.keySet());
        assertEquals(asSet("rule1", "rule2"), namesOf(env.getLocalRules()));
    }

    public void testPrograms() throws Exception {
        parseEnv("program P1 goto 0");
        assertEquals(asSet("program:P1_total", "program:P1_partial"), proofObligations.keySet());
        assertEquals(asSet("P1"), namesOf(env.getLocalPrograms()));
    }

    public void testLemmaPO() throws Exception {
        parseEnv("lemma lemma1 1=2->2=3");
        assertEquals(makeTerm("1=2 -> 2=3"), proofObligations.get("lemma:lemma1").getProblemTerm());
    }

    public void testRulePO() throws Exception {
        parseEnv("rule rule1 find 1=2 replace 2=3 add 4=5 |-");
        assertEquals(makeTerm("!((1=2) = (2=3) -> !4=5)"),
                proofObligations.get("rule:rule1").getProblemTerm());
    }

    public void testProgramPO() throws Exception {
        parseEnv("program P1 goto 0");
        assertEquals(makeTerm("[[0;P1]]true"),
                proofObligations.get("program:P1_total").getProblemTerm());
        assertEquals(makeTerm("[0;P1]true"),
                proofObligations.get("program:P1_partial").getProblemTerm());
    }

    public void testLemmaProofObligationEnv() throws Exception {

        parseEnv("lemma l1 true axiom ax1 true lemma l2 true");

        assertEquals(2, proofObligations.size());

        Environment envL1 = proofObligations.get("lemma:l1").getProofEnvironment();
        Environment envL2 = proofObligations.get("lemma:l2").getProofEnvironment();

        assertEquals(asSet(), namesOf(envL1.getLocalLemmas()));
        assertEquals(asSet("l1", "ax1"), namesOf(envL2.getLocalLemmas()));
    }

    public void testRuleProofObligationEnv() throws Exception {

        parseEnv("rule r1 add true |- tags asAxiom \n" +
                "axiom rule rax1 closegoal\n" +
                "rule r2 add true |-");

        assertEquals(2, proofObligations.size());

        Environment envL1 = proofObligations.get("rule:r1").getProofEnvironment();
        Environment envL2 = proofObligations.get("rule:r2").getProofEnvironment();

        assertEquals(asSet(), namesOf(envL1.getLocalLemmas()));
        assertEquals(asSet(), namesOf(envL1.getLocalRules()));
        assertEquals(asSet("r1"), namesOf(envL2.getLocalLemmas()));
        assertEquals(asSet("r1", "rax1"), namesOf(envL2.getLocalRules()));
    }


}
