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

import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

public class TestMetaFunctions extends TestCaseWithEnv {
    MutableRuleApplication ra;
    MetaEvaluator eval;
    
    protected void setUp() throws Exception {
//        Handler.registerNoneHandler();
        env = new Environment("none:wrapped_for_skolem", DEFAULT_ENV);
        ra = new MutableRuleApplication();
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
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    // was a bug.
    public void testSubstInProg() throws Exception {
        Term t = makeTerm("$$subst(\\var b, true, [0;test_meta_functions_subst])");
        assertEvalsTo(t, "[0;test_meta_functions_subst']");
        {
            Term argTerm = env.getProgram("test_meta_functions_subst'").
                    getStatement(0).getSubterms().get(0);
            assertEquals(makeTerm("true"), argTerm);
        }

        {
            Term argTerm = env.getProgram("test_meta_functions_subst").
                    getStatement(0).getSubterms().get(0);
            assertEquals(makeTerm("\\var b as bool"), argTerm);
        }
    }
    
    public void testSubstInProg2() throws Exception {
        // b does not appear unbound in P ==> should remain [0;P]
        Term t = makeTerm("$$subst(\\var b, true, [0; P])");
        assertEvalsTo(t, "[0;P]");
    }

    // was a bug
    public void testSubstInProg3() throws Exception {
        // program: b1 := \var b
        Term t = makeTerm("$$subst(\\var b, true, [0;test_meta_functions_subst2])");
        assertEvalsTo(t, "[0;test_meta_functions_subst2']");
        {
            AssignmentStatement stm =
                    (AssignmentStatement) env.getProgram("test_meta_functions_subst2'").
                            getStatement(0);
            
            Assignment ass = stm.getAssignments().get(0);
            List<Term> subterms = stm.getSubterms();
            
            assertEquals(ass.getValue(), subterms.get(1));
        }
    }
    
    public void testBoundSubst() throws Exception {
        Term t = makeTerm("(\\bind x; $$subst(x, 3, x + (\\bind x; x*2)))");
        assertEvalsTo(t.getSubterm(0), "3 + (\\bind x; x*2)");
    }
 
    public void testSpec() throws Exception {
        try {
            assertEvalsTo("$$polymorphicSpec(arb as int, arb as bool, bf(arb as int), false)",
                "bf(arb as bool)");
            fail("only type variables can be specialised");
        } catch (TermException ex) {
            if (VERBOSE)
                ex.printStackTrace();
        }
        
        
        assertEvalsTo(
                "$$polymorphicSpec(arb as 'a, arb as bool, bf(arb as 'a), false)",
                "bf(arb as bool)");
        
        assertEvalsTo(
                "$$polymorphicSpec(arb as 'a, arb as int, (\\forall x as 'a; id(x) = x), false)",
                "(\\forall x as int; id(x) = x)");
        
        assertEvalsTo(
                "$$polymorphicSpec(arb as 'a, arb as int, (\\T_all 'a; id(arb as 'a) = arb as 'a), false)",
                "(\\T_all 'a; id(arb as 'a) = arb as 'a)");

        Term.SHOW_TYPES = true;
        // see visit(Binder) in SpecialiseMetaFunction!
        Term t = makeTerm("(\\bind x as 'a; $$polymorphicSpec(x, 0, (bf(x) & (\\forall x as 'a; bf(x))), true))");
        assertEvalsTo(t.getSubterm(0),
                "bf(0) & (\\forall x as int; bf(x))");
    
    }
    
    public void testSkolem() throws Exception {
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term result2 = eval.evalutate(t);
        
        assertEquals(makeTerm("sk as int"), result);
        assertEquals(makeTerm("sk as int"), result2);
    }
    
    public void testSkolemDifferent() throws Exception {
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term t2 = makeTerm("$$skolem(2)");
        Term result2 = eval.evalutate(t2);
        
        assertEquals(makeTerm("sk as int"), result);
        assertEquals(makeTerm("sk1 as int"), result2);
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
        
        assertEquals(makeTerm("sk100 as int"), result);
        assertEquals(makeTerm("sk100 as int"), result2);
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
        assertEvalsTo("$$incPrg([1;P])", "[2;P]");
        assertEvalsTo("$$incPrg([[100; Q]])", "[[101;Q]]");
    }
    
    public void testJmpPrg() throws Exception {
        assertEvalsTo("$$jmpPrg([1;P], 0)", "[0;P]");
        assertEvalsTo("$$jmpPrg([[100 ; Q]], 99)", "[[99 ; Q]]");
    }
    
}