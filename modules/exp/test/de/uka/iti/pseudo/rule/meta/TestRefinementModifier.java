package de.uka.iti.pseudo.rule.meta;

import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.prettyprint.AnnotatedString;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor.DepthTermVisitor;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Dump;
import de.uka.iti.pseudo.util.Triple;

public class TestRefinementModifier extends TestCaseWithEnv {

    private Map<String, ProofObligation> problems;
    private Term inivar;
//    private Function markAbs;
//    private Function markConcr;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Triple<Environment, Map<String, ProofObligation>, Map<String, ProofScript>> r =
                makeEnvAndProofObls(getClass().getResource("refinementMod.test.p"));
        env = r.fst();
        problems = r.snd();
        this.inivar = makeTerm("inivar");

//        markAbs = env.getFunction("$a");
//        markConcr = env.getFunction("$c");
    }

    private void assertEqualTerms(Term t1, Term t2) {
        if(!t1.equals(t2)) {
            PrettyPrint pp = new PrettyPrint(env);
            AnnotatedString s1 = pp.print(t1, 80);
            System.out.println(s1);
            System.out.println("---");
            AnnotatedString s2 = pp.print(t2, 80);
            System.out.println(s2);
            assertEquals(s1.toString(), s2.toString());
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
            Dump.dumpProgram(expected);
            Dump.dumpProgram(program);
            throw e;
        }
    }


    private Term getProblem(String string) throws EnvironmentException {
        return problems.get("refineMod").initProof().
                getRoot().getSequent().getSuccedent().get(0);
    }

    public void testRefinementMod() throws Exception {
        Term problem = getProblem("refineMod");
        RefinementModifier rmod = new RefinementModifier(env, problem, inivar);
        rmod.setMarkFunctions(env.getFunction("$markA"), env.getFunction("$markC"));

        Term result = rmod.apply();

        assertNotNull(env.getProgram("A'"));
        assertNotNull(env.getProgram("C'"));

        assertEqualProgs(env.getProgram("A_expected"), env.getProgram("A'"));
        assertEqualProgs(env.getProgram("C_expected"), env.getProgram("C'"));

//        PrettyPrint pp = new PrettyPrint(env);
//        System.out.println(pp.print(result));

        assertEqualTerms(makeTerm(makeResultTerm(0, 1, false, "inivar as int", null) + " & "
                 + makeResultTerm(7, 4, true, "11", "x=1") + " & "
                 + makeResultTerm(4, 8, true, "22", "x=2") + ""), result);
    }

    public void testTerminatingRefinementMod() throws Exception {
        Term problem = getProblem("terminatingRefMod");
        RefinementModifier rmod = new RefinementModifier(env, problem, inivar);
        rmod.setMarkFunctions(env.getFunction("$markA"), env.getFunction("$markC"));

        Term result = rmod.apply();
        final Program cPrime = env.getProgram("C'");

        PrettyPrint pp = new PrettyPrint(env);
        System.out.println(pp.print(result,40));

        DepthTermVisitor v = new DepthTermVisitor() {
            @Override
            public void visit(de.uka.iti.pseudo.term.LiteralProgramTerm ltp) throws TermException {
                super.visit(ltp);
                if(ltp.getProgram() == cPrime) {
                    assertEquals(Modality.BOX_TERMINATION, ltp.getModality());
                }
            }
        };

        result.visit(v);
    }

    private String makeResultTerm(int concr, int abs, boolean anon, String var, String pre) {
        String glue = "($markA = $markC & ($markA = 1 -> x=1 & 11 &< var) & " +
        		"($markA = 2 -> x=2 & 22 &< var) & ($markA = 0 -> x =0))";
        String update =  anon ? "{ x := x1 || y := y1 }" :"";
        String anUpd = "{ $markA := 0 || $markC := 0}";
        String varUpd = var != null ? "{ var := " + var + "}" :"";
        String assum = pre != null ? pre + " -> " : "";
        return update + anUpd + varUpd + "(" + assum + "[" + concr + ";C'][<" + abs + ";A'>]" + glue + ")";
    }

    public void testUsingTheMarks() throws Exception {
        Term problem = getProblem("using");
        RefinementModifier rmod = new RefinementModifier(env, problem, inivar);

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
        Term problem = getProblem("using2");
        // deliberately without variant!
        RefinementModifier rmod = new RefinementModifier(env, problem, Environment.getFalse());
        rmod.setMarkFunctions(env.getFunction("$markA"), env.getFunction("$markC"));

        Term result = rmod.apply();
        System.out.println(result);
        String glue = "($markA = $markC & ($markA = 1 -> true & true) & ($markA = 0 -> x=0 ))";
        Term exp = makeTerm("{ $markA := 0 || $markC := 0 }[0;C_1'][<0;A_1'>]" + glue +
                "&\n { $markA := 0 || $markC := 0 }[2;C_1'][<2;A_1'>]" + glue);

        assertEquals(2, env.getProgram("C_1'").countStatements());
        assertEquals(2, env.getProgram("A_1'").countStatements());
        assertEqualTerms(exp, result);
    }

    public void testUsingTheMarks3() throws Exception {
        Term problem = getProblem("using3");
        RefinementModifier rmod = new RefinementModifier(env, problem, inivar);

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
