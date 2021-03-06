/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.io.StringReader;
import java.util.Map;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.AbstractMetaFunction;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Lemma;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.creation.EnvironmentMaker;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.util.Dump;

public class TestFileParser extends TestCaseWithEnv {

    private Environment testEnv(String string) throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " + string), "*test*");
//        de.uka.iti.pseudo.util.protocol.none.Handler.registerNoneHandler();
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Environment env = em.getEnvironment();
        // env.dump();
        return env;
    }

    private void assertEnvFail(String string) throws Exception {
        try {
            testEnv(string);
            fail("should fail");
        } catch(ASTVisitException e) {
            // this should happen
            if(TestCaseWithEnv.VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testIntLoadable() throws Exception {
        testEnv("include \"$int.p\"");
    }

    public void testPolymorphSorts() throws Exception {
        Environment env = testEnv("sort poly('a, 'b)");
        assertEquals("Sort[poly;2]", env.getSort("poly").toString());

        env = testEnv("sort poly('a, 'b)");
        assertEquals("Sort[poly;2]", env.getSort("poly").toString());
    }

    public void testAssignableFunctions() throws Exception {
        // assignables must be nullary
        assertEnvFail("function int f(int) assignable");
        // assignables must not have type vars in type
        assertEnvFail("function 'a f assignable");
        assertEnvFail("sort S('a) function S('a) f assignable");
    }

    // from syntax errors
    public void testRules2() throws Exception {
        testEnv("function int f(int) rule r find f(0)=0 |- samegoal replace 1=f(1) as int");
        testEnv("lemma testLemma arb as int =1");
    }

    public void testRules() throws Exception {
        // no replace in newgoals (was a bug)
        assertEnvFail("rule something find %a newgoal replace %b");
        assertEnvFail("rule something find 1 assume %b |- samegoal replace %b+1");
    }

    public void testAxiomRule() throws Exception {
        Environment env1 = testEnv(
                "axiom rule axiomRule find true replace true\n" +
                "rule nonAxiomRule find true replace true");
        Rule axiomRule = env1.getRule("axiomRule");
        Rule nonAxiomRule = env1.getRule("nonAxiomRule");
        assertNotNull(axiomRule);
        assertNotNull(nonAxiomRule);
        assertNotNull(axiomRule.getProperty("axiom"));
        assertNull(nonAxiomRule.getProperty("axiom"));
    }

    // test findless rules
    public void testFindless() throws Exception {
        testEnv("rule cut_findless samegoal add |- %b samegoal add %b |-");
        assertEnvFail("rule may_not_replace samegoal replace %b");
        assertEnvFail("rule may_not_remove samegoal remove");
    }

    // was a bug
    public void testDoubleReplace() throws Exception {
        assertEnvFail("rule doubleReplace find true samegoal replace true replace false");
    }

    // due to a bug
    public void testWhereCondTyping() throws Exception {
        env = testEnv("rule ext find %h=%h2 as int\n" +
        		"where freshTypeVar type as %'a, %h, %h2\n" +
        		"replace (\\T_all %'a; true)");
        Rule r = env.getRule("ext");
        WhereClause wc = r.getWhereClauses().get(0);
        Term arg = wc.getArguments().get(0);
        assertEquals(makeTerm("type as %'a"), arg);
    }

    public void testFindReplaceSameType() throws Exception {
        assertEnvFail("rule something find 1 newgoal replace true");

        Environment e = testEnv("rule something find 1 samegoal replace %a");
    }

    public void testAxioms() throws Exception {
        Environment e = testEnv("axiom axiom1 true axiom axiom2 false->false");

        assertEnvFail("axiom schemas %a");
        assertEnvFail("sort S function S const axiom notbool const");
    }

    public static class MockMeta extends AbstractMetaFunction {

        public MockMeta() throws EnvironmentException {
            super(Environment.getBoolType(), "$$mock", new Type[0]);
        }

        @Override
        public Term evaluate(Application application, Environment env,
                RuleApplication ruleApp) throws TermException {
            return Environment.getTrue();
        }

    }

    public void testMetaInAxiom() throws Exception {
        assertEnvFail("plugins metaFunction \"de.uka.iti.pseudo.parser.file.TestFileParser$MockMeta\"" +
        		"axiom metaAxiom $$mock");
    }

    // revealed a bug
    public void testFileProperties() throws Exception {
        Environment e = testEnv("properties prop \"value\"");
        assertEquals("value", e.getProperty("prop"));

        String string = "properties quoted \"\\\\Quotes: \\\"Hello\\\"\"";
        e = testEnv(string);
        assertEquals("\\Quotes: \"Hello\"", e.getProperty("quoted"));
    }

    // assumed bug.
    public void testRuleProperties() throws Exception {
        Environment e = testEnv("rule two_props closegoal tags Tag1 \"value1\" Tag2");
        Rule rule = e.getRule("two_props");
        assertEquals("value1", rule.getProperty("Tag1"));
        assertEquals("", rule.getProperty("Tag2"));
    }

    public void testRulePropertiesWithQuotes() throws Exception {
        // Is in reality: tags quoted "\\Quotes: \"Hello\""
        String string = "rule quotes closegoal tags quoted \"\\\\Quotes: \\\"Hello\\\"\"";
//        System.out.println(string);
        Environment e = testEnv(string);
        Rule rule = e.getRule("quotes");
        assertEquals("\\Quotes: \"Hello\"", rule.getProperty("quoted"));
    }

    // due to problems with cuts
    public void testGoalActionNaming() throws Exception {
        Environment e = testEnv("rule something find 1 samegoal \"actionname\" replace 2");
        assertEquals("actionname", e.getRule("something").getGoalActions().get(0).getName());
    }

    public void testProtectedProduction() throws Exception {
        testEnv("function `int function(int)` rule `find` find `function(0)` samegoal replace `function(1) as int`");
        testEnv("lemma `lemma` `1` `` ` =1`");
    }

    public void testPrograms() throws Exception {
        Environment e = testEnv("program P assert true program Q assume true");
        if(TestCaseWithEnv.VERBOSE) {
            Dump.dumpEnv(e);
        }
        assertNotNull(e.getProgram("P"));
        assertNotNull(e.getProgram("Q"));
        assertNull(e.getProgram("Unknown"));
    }

    public void testProgramWithSchema() throws Exception {
        assertEnvFail("program P assert [%a : skip]true");
        assertEnvFail("program P assert (\\var b) as bool");
        assertEnvFail("program P assert %b");
        assertEnvFail("program P assert arb = arb");
        assertEnvFail("program Q end program P assert [0;Q]%b");
    }

    public void testProgramTextAnnotation() throws Exception {
        Environment e = testEnv("program P skip skip; \"hello world\" skip");
        if(TestCaseWithEnv.VERBOSE) {
            Dump.dumpEnv(e);
        }
        Program P = e.getProgram("P");
        assertNull(P.getTextAnnotation(0));
        assertEquals("hello world", P.getTextAnnotation(1));
        assertNull(P.getTextAnnotation(2));
        assertNull(P.getTextAnnotation(3));

    }

    // no longer supported :-(
//    public void testProblemSequent() throws Exception {
//        Parser fp = new Parser();
//        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " +
//        		"function bool b  problem b, true |- b, false"), "*test*");
//        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
//        Map<String, ProofObligation> problems = em.getProofObligations();
//        assertNotNull(problems);
//        assertEquals("b, true |- b, false", problems.get("").toString());
//    }

    public void testProblemNotSequent() throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " +
                        "lemma trueLemma true"), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Map<String, ProofObligation> problems = em.getProofObligations();
        assertNotNull(problems);
        assertEquals(1, problems.size());
        ProofObligation proofObligation = problems.get("lemma:trueLemma");
        assertNotNull(proofObligation);
        assertEquals("true", proofObligation.getProblemTerm().toString());
    }

    public void testTwoNamedProblems() throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " +
                "lemma problemA true   lemma problemB false"), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Map<String, ProofObligation> problems = em.getProofObligations();
        assertEquals(2, problems.size());
        ProofObligation problemA = problems.get("lemma:problemA");
        assertNotNull(problemA);
        assertEquals("lemma:problemA", problemA.getName());
        assertEquals(Environment.getTrue(), problemA.getProblemTerm());
        ProofObligation problemB = problems.get("lemma:problemB");
        assertNotNull(problemB);
        assertEquals(Environment.getFalse(), problemB.getProblemTerm());
    }

    // detected a bug
    public void testTwoLemmasWithSameName() throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " +
                "lemma samename true   lemma samename false"), "*test*");
        try {
            new EnvironmentMaker(fp, ast, "none:test");
            fail("Should fail: 2 unnamed problems");
        } catch (ASTVisitException e) {
            if(TestCaseWithEnv.VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testAxiomLemmas() throws Exception {
        Environment env1 = testEnv(
                "axiom axiomLemma true\n" +
                "lemma nonAxiomLemma true");
        Lemma axiomLemma = env1.getLemma("axiomLemma");
        Lemma nonAxiomLemma = env1.getLemma("nonAxiomLemma");
        assertNotNull(axiomLemma);
        assertNotNull(nonAxiomLemma);
        assertNotNull(axiomLemma.getProperty("axiom"));
        assertNull(nonAxiomLemma.getProperty("axiom"));
    }

    // does not apply any longer
//    public void testLateUnnamedProblems() throws Exception {
//        Parser fp = new Parser();
//        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " +
//                "problem problem_A : true   problem true"), "*test*");
//        try {
//            new EnvironmentMaker(fp, ast, "none:test");
//            fail("Should fail: Late unnamed problems");
//        } catch (ASTVisitException e) {
//            if(TestCaseWithEnv.VERBOSE) {
//                e.printStackTrace();
//            }
//        }
//    }


    // no problem, only 2 programs
    public void testAutoProblems() throws Exception {
        Parser fp = new Parser();
        ASTFile ast = fp.parseFile(new StringReader("include \"$base.p\" " +
                "program P1 end " +
                "program P2 end"), "*test*");
        EnvironmentMaker em = new EnvironmentMaker(fp, ast, "none:test");
        Map<String, ProofObligation> problems = em.getProofObligations();
        assertEquals(4, problems.size());
        assertTrue(problems.containsKey("program:P1_total"));
        assertTrue(problems.containsKey("program:P2_total"));
        assertTrue(problems.containsKey("program:P1_partial"));
        assertTrue(problems.containsKey("program:P2_partial"));

        Term term = problems.get("program:P1_partial").getProblemTerm();
        assertEquals("[0;P1]true", term.toString(false));

        term = problems.get("program:P1_total").getProblemTerm();
        assertEquals("[[0;P1]]true", term.toString(false));

    }

    public void testUnknownService() throws Exception {
        try {
            Environment e = testEnv("plugins uknownService \"java.lang.Object\"");
            Dump.dumpEnv(e);
            fail("Should have failed!");
        } catch (ASTVisitException e) {
            assertTrue(e.getMessage().startsWith("Unknown service"));
        }
    }

}
