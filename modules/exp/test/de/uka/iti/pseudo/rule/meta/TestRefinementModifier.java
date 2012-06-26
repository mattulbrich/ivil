package de.uka.iti.pseudo.rule.meta;

import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Pair;

public class TestRefinementModifier extends TestCaseWithEnv {

    private Map<String, Sequent> problems;
//    private Function markAbs;
//    private Function markConcr;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Pair<Environment, Map<String, Sequent>> r =
                makeEnvAndProblems(getClass().getResource("refinementMod.test.p"));
        env = r.fst();
        problems = r.snd();

//        markAbs = env.getFunction("$a");
//        markConcr = env.getFunction("$c");
    }

    private static void assertEqualTerms(Term t1, Term t2) {
        if(!t1.equals(t2)) {
            assertEquals(t1.toString(true), t2.toString(true));
        }
    }

    private static void assertEqualProgs(Program expected, Program program) {
        try {
            List<Statement> s1 = expected.getStatements();
            List<Statement> s2 = program.getStatements();
            assertEquals("Lengths do not match.", s1.size(), s2.size());
            for (int i = 0; i < s1.size(); i++) {
                // System.out.println("P1: " + s1.get(i));
                // System.out.println("P2: " + s2.get(i));
                assertEquals(s1.get(i), s2.get(i));
            }
        } catch (AssertionFailedError e) {
            expected.dump();
            program.dump();
            throw e;
        }
    }


    public void testRefinementMod() throws Exception {
        Term problem = problems.get("refineMod").getSuccedent().get(0);
        RefinementModifier rmod = new RefinementModifier(env, problem);
        rmod.setMarkFunctions(env.getFunction("$markA"), env.getFunction("$markC"));

        Term result = rmod.apply();

        assertNotNull(env.getProgram("A'"));
        assertNotNull(env.getProgram("C'"));

        assertEqualProgs(env.getProgram("A_expected"), env.getProgram("A'"));
        assertEqualProgs(env.getProgram("C_expected"), env.getProgram("C'"));

//        PrettyPrint pp = new PrettyPrint(env);
//        System.out.println(pp.print(result));

        assertEqualTerms(makeTerm(makeResultTerm(0, 1, false, null) + " & " +
                makeResultTerm(7, 4, true, "11") + " & " +
                makeResultTerm(4, 8, true, "22") + ""), result);
    }

    private String makeResultTerm(int concr, int abs, boolean anon, String var) {
        String glue = "($markA = $markC & ($markA = 1 -> x=1) & ($markA = 2 -> x=2) & ($markA = 0 -> x =0))";
        String update =  anon ? "{ y := y1 || x := x1 }" :"";
        String anUpd = "{ $markA := 0 || $markC := 0}";
        String varUpd = var != null ? "{ var := " + var + "}" :"";
        return update + anUpd + varUpd + "[" + concr + ";C'][<" + abs + ";A'>]" + glue;
    }

    public void testUsingTheMarks() throws Exception {
        Term problem = problems.get("using").getSuccedent().get(0);
        RefinementModifier rmod = new RefinementModifier(env, problem);

        try {
            Term result = rmod.apply();
            System.out.println(result);
            fail("Should have failed: Missing literal!");
        } catch (TermException e) {
            assertTrue(e.getMessage().contains("a skip refinement marker needs at least 2 arguments"));
            if(VERBOSE) {
                e.printStackTrace();
            }
        }

    }

    public void testUsingTheMarks2() throws Exception {
        Term problem = problems.get("using2").getSuccedent().get(0);
        RefinementModifier rmod = new RefinementModifier(env, problem);
        rmod.setMarkFunctions(env.getFunction("$markA"), env.getFunction("$markC"));

        Term result = rmod.apply();
        System.out.println(result);
        String glue = "($markA = $markC & ($markA = 0 -> x=0))";
        Term exp = makeTerm("{ $markA := 0 || $markC := 0 }[0;C_1'][<0;A_1'>]" + glue +
                "&\n {}{ $markA := 0 || $markC := 0 }[2;C_1'][<2;A_1'>]" + glue);
        assertEquals(2, env.getProgram("C_1'").countStatements());
        assertEquals(2, env.getProgram("A_1'").countStatements());
        assertEqualTerms(exp, result);
    }

    public void testUsingTheMarks3() throws Exception {
        Term problem = problems.get("using3").getSuccedent().get(0);
        RefinementModifier rmod = new RefinementModifier(env, problem);

        try {
            Term result = rmod.apply();
            System.out.println(result);
            fail("Should have failed: integer instead of boolean!");
        } catch (TermException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
            assertTrue(e.getMessage().contains("refinement marker needs boolean"));
        }

    }

//    public void testAsymmetricSet() throws Exception {
//        Term problem = problems.get("asymm").getSuccedent().get(0);
//        RefinementModifier rmod = new RefinementModifier(env, problem);
//
//        try {
//            Term result = rmod.apply();
//            System.out.println(result);
//            fail("Should have failed: asymmetric mark set!");
//        } catch (TermException e) {
//            if(VERBOSE) {
//                e.printStackTrace();
//            }
//        }
//    }
}
