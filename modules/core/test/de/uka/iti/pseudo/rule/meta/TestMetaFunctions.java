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
package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.LocalSymbolTable;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class TestMetaFunctions extends TestCaseWithEnv {
    MutableRuleApplication ra;
    MetaEvaluator eval;

    @Override
    protected void setUp() throws Exception {
//        Handler.registerNoneHandler();
        env = new Environment("none:wrapped_for_skolem", DEFAULT_ENV);
        ra = new MutableRuleApplication();
        Proof p = new Proof(Environment.getTrue());
        ra.setProofNode(p.getRoot());
        eval = new MetaEvaluator(ra, env);
    }

    private void assertEvalsTo(String t1, String t2) throws Exception {
        Term t = makeTerm(t1);
        assertEvalsTo(t, t2);
    }

    private void assertEvalsTo(Term t1, String t2) throws Exception {
        Term evalutation = eval.evalutate(t1);
//        assertEquals(makeTerm(t2), evaluation);
        assertEquals(makeTerm(t2).toString(true), evalutation.toString(true));
    }

    public void testSubst() throws Exception {
        Term t = makeTerm("(\\bind x; $$subst(x, 3, i1+x))");
        assertEvalsTo(t.getSubterm(0), "i1+3");

        try {
            assertEvalsTo("$$subst(i1,i2,false)", "false");
            fail("Only variables can be substituted");
        } catch (TermException e) {
            if(VERBOSE) {
                e.printStackTrace();
            }
        }
    }

    public void testBoundSubst() throws Exception {
        Term t = makeTerm("$$subst(\\var x, 3, \\var x + (\\bind x; x * 2))");
        assertEvalsTo(t, "3 + (\\bind x; x*2)");
    }

    public void testPolymorphicSubst() throws Exception {
        try {
            assertEvalsTo("$$polymorphicSubst(\\var x as int, arb as bool, bf(arb as int))",
                "bf(arb as bool)");
            fail("only type variables can be specialised");
        } catch (TermException ex) {
            out(ex);
        }

        assertEvalsTo(
                "$$polymorphicSubst(\\var x as 'a, arb as bool, bf(\\var x as 'a))",
                "bf(arb as bool)");

        assertEvalsTo(
                "$$polymorphicSubst(\\var x as 'a, arb as int, (\\forall x as 'a; id(x) = x))",
                "(\\forall x as int; id(x) = x)");

        assertEvalsTo(
                "$$polymorphicSubst(\\var x as 'a, arb as int, (\\T_all 'a; id(\\var x as 'a) = arb as 'a))",
                "(\\T_all 'a; id(\\var x as 'a) = arb as 'a)");

//        Term.SHOW_TYPES = true;
        // see visit(Binder) in SpecialiseMetaFunction!
        assertEvalsTo(
                "$$polymorphicSubst(\\var x as 'a, 0, bf(\\var x as 'a) & (\\forall x as 'a; bf(x)))",
                "bf(0) & (\\forall x as int; bf(x as int))");
    }

    public void testTypeUnification() throws Exception {

        assertEvalsTo(
                "$$unifyTypes(type as 'a, 0, bf(arb as 'a))",
                "bf(arb as int)");

        assertEvalsTo(
                "$$unifyTypes(type as poly('a, int), type as poly(bool, 'b), " +
                   "bf(arb as 'a) & bf(arb as 'b))",
                "bf(arb as bool) & bf(arb as int)");

    }

//    public void testSkolemWithoutProofNode() throws Exception {
//        ra.setProofNode(null);
//        Term t = makeTerm("$$skolem(1)");
//        try {
//            Term result = eval.evalutate(t);
//            fail("Should not work (missing locals), but " + result);
//        } catch (TermException e) {
//            if(VERBOSE) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void testSkolem() throws Exception {
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term result2 = eval.evalutate(t);

        LocalSymbolTable lst = eval.getLocalSymbolTable();
        assertNotNull(lst.getFunction("sk"));
        assertEquals(makeTerm("sk as int", lst), result);
        assertEquals(makeTerm("sk as int", lst), result2);
    }

    // revealed a regression bug
    public void testSkolemDifferent() throws Exception {
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term t2 = makeTerm("$$skolem(2)");
        Term result2 = eval.evalutate(t2);

        LocalSymbolTable lst = eval.getLocalSymbolTable();
        assertNotNull(lst.getFunction("sk"));
        assertNotNull(lst.getFunction("sk1"));
        assertEquals(makeTerm("sk as int", lst), result);
        assertEquals(makeTerm("sk1 as int", lst), result2);
    }

    public void testSkolemNames() throws Exception {
        Term t = makeTerm("$$skolem(i1)");
        Term result = eval.evalutate(t);

        assertEquals("i11 as int", result.toString(true));
    }

    public void testSkolemReplay() throws Exception {
        ra.getProperties().put("skolemName(1 as int)", "sk100");

        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term result2 = eval.evalutate(t);

        LocalSymbolTable lst = eval.getLocalSymbolTable();
        assertEquals(makeTerm("sk100 as int", lst), result);
        assertEquals(makeTerm("sk100 as int", lst), result2);
    }

    public void testIntEval() throws Exception {
        assertEvalsTo("$$intEval(2+3)", "5");
        assertEvalsTo("$$intEval(2-3)", "-1");

        assertEvalsTo("$$intEval(3/2)", "1");
        assertEvalsTo("$$intEval(2/3)", "0");
        assertEvalsTo("$$intEval(2*3)", "6");
        assertEvalsTo("$$intEval($mod(7,3))", "1");

        try {
            Term t = makeTerm("$$intEval(i1+3)");
            Term result = eval.evalutate(t);
            fail("should fail but is " + result);
        } catch(TermException ex) {}
    }

    public void testResolveUnique() throws Exception {
        assertEvalsTo("$$resolveUnique(uniq1, uniq1)", "true");
        assertEvalsTo("$$resolveUnique(uniq1, uniq2)", "false");
        assertEvalsTo("$$resolveUnique(uniq3(0,0), uniq4(0,0))", "false");
        assertEvalsTo("$$resolveUnique(uniq3(i1,0), uniq3(i3,0))", "i1=i3 & 0=0");

        try {
            Term t = makeTerm("$$resolveUnique(i1, uniq1)");
            Term result = eval.evalutate(t);
            fail("should fail but is " + result);
        } catch(TermException ex) {}
    }

    public void testIntEvalNegated() throws Exception {
        assertEvalsTo("$$intEval((-2)+(-3))", "-5");
    }

    public void testIncPrg() throws Exception {
        assertEvalsTo("$$incPrg([1;P]%a)", "[2;P]%a");
        assertEvalsTo("$$incPrg([[100; Q]]b1)", "[[101;Q]]b1");
    }

    public void testJmpPrg() throws Exception {
        assertEvalsTo("$$jmpPrg([1;P]true, 0)", "[0;P]true");
        assertEvalsTo("$$jmpPrg([[100 ; Q]]b2, 99)", "[[99 ; Q]]b2");
    }


    // NO MORE FREE VARS IN PROGRAMS
//    // was a bug.
//    public void testSubstInProg() throws Exception {
//        Term t = makeTerm("$$subst(\\var b, true, [0;test_meta_functions_subst]true)");
//        assertEvalsTo(t, "[0;test_meta_functions_subst']true");
//        {
//            Term argTerm = env.getProgram("test_meta_functions_subst'").
//                    getStatement(0).getSubterms().get(0);
//            assertEquals(makeTerm("true"), argTerm);
//        }
//
//        {
//            Term argTerm = env.getProgram("test_meta_functions_subst").
//                    getStatement(0).getSubterms().get(0);
//            assertEquals(makeTerm("\\var b as bool"), argTerm);
//        }
//    }
//
//    public void testSubstInProg2() throws Exception {
//        // b does not appear unbound in P ==> should remain [0;P]
//        Term t = makeTerm("$$subst(\\var b, true, [0; P](\\var b))");
//        assertEvalsTo(t, "[0;P]true");
//    }
//
//    // was a bug
//    public void testSubstInProg3() throws Exception {
//        // program: b1 := \var b
//        Term t = makeTerm("$$subst(\\var b, true, [0;test_meta_functions_subst2]true)");
//        assertEvalsTo(t, "[0;test_meta_functions_subst2']true");
//        {
//            AssignmentStatement stm =
//                    (AssignmentStatement) env.getProgram("test_meta_functions_subst2'").
//                            getStatement(0);
//
//            Assignment ass = stm.getAssignments().get(0);
//            List<Term> subterms = stm.getSubterms();
//
//            assertEquals(ass.getValue(), subterms.get(1));
//        }
//    }
//
//    public void testSubstInProgramNested() throws Exception {
//        // test_meta_functions_subst: assert \var b
//        // test_meta_functions_subst3: assert [0;test_meta_functions_subst]
//
//        Term t = makeTerm("$$subst(\\var b, true, [0;test_meta_functions_subst3]true)");
//        assertEvalsTo(t, "[0;test_meta_functions_subst3']true");
//
//        Program program = env.getProgram("test_meta_functions_subst'");
//        assertNotNull(program);
//
//        {
//            Statement stm =
//                    env.getProgram("test_meta_functions_subst3'").
//                            getStatement(0);
//
//            LiteralProgramTerm subterm = (LiteralProgramTerm) stm.getSubterms().get(0);
//            assertEquals(program, subterm.getProgram());
//        }
//        {
//            Statement stm =
//                env.getProgram("test_meta_functions_subst'").
//                        getStatement(0);
//            Term cond = stm.getSubterms().get(0);
//            assertEquals(makeTerm("true"), cond);
//        }
//    }
}