package de.uka.iti.pseudo.rule.meta;

import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Pair;

public class TestRefinementModifier extends TestCaseWithEnv {

    private Map<String, Sequent> problems;
    private Function markAbs;
    private Function markConcr;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Pair<Environment, Map<String, Sequent>> r =
                makeEnvAndProblems(getClass().getResource("refinementMod.test.p"));
        env = r.fst();
        problems = r.snd();

        markAbs = env.getFunction("$a");
        markConcr = env.getFunction("$c");
    }

    private String makeResultTerm(int concr, int abs) {
        String glue = "($a = $c & ($a = 1 -> x=1) & ($a = 2 -> x=2) & ($a = 0 -> x =0))";
        String update = "{ y := y1 || x := x1 || $c := 0 || $a := 0}";
        return update + "[" + concr + ";C'][<" + abs + ";A'>]" + glue;
    }

    private void assertEqualTerms(Term t1, Term t2) {
        if(!t1.equals(t2)) {
            assertEquals(t1.toString(true), t2.toString(true));
        }
    }

    private void assertEqualProgs(Program expected, Program program) {
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
        RefinementModifier rmod = new RefinementModifier(env, problem, markAbs, markConcr);

        Term result = rmod.apply();

        assertNotNull(env.getProgram("A'"));
        assertNotNull(env.getProgram("C'"));

        assertEqualProgs(env.getProgram("A_expected"), env.getProgram("A'"));
        assertEqualProgs(env.getProgram("C_expected"), env.getProgram("C'"));

//        PrettyPrint pp = new PrettyPrint(env);
//        System.out.println(pp.print(result));

        assertEqualTerms(makeTerm(makeResultTerm(0, 1) + " & " +
                makeResultTerm(7, 4) + " & " +
                makeResultTerm(4, 8) + ""), result);
    }

    public void testUsingTheMarks() throws Exception {
        Term problem = problems.get("using").getSuccedent().get(0);
        RefinementModifier rmod = new RefinementModifier(env, problem, markAbs, markConcr);

        try {
            Term result = rmod.apply();
            System.out.println(result);
            fail("Should have failed: reading $a and $c!");
        } catch (TermException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }

    }

    public void testUsingTheMarks2() throws Exception {
        Term problem = problems.get("using2").getSuccedent().get(0);
        RefinementModifier rmod = new RefinementModifier(env, problem, markAbs, markConcr);

        try {
            Term result = rmod.apply();
            System.out.println(result);
            fail("Should have failed: setting $a in C!");
        } catch (TermException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }

    }

    public void testAsymmetricSet() throws Exception {
        Term problem = problems.get("asymm").getSuccedent().get(0);
        RefinementModifier rmod = new RefinementModifier(env, problem, markAbs, markConcr);

        try {
            Term result = rmod.apply();
            System.out.println(result);
            fail("Should have failed: asymmetric mark set!");
        } catch (TermException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }
}
