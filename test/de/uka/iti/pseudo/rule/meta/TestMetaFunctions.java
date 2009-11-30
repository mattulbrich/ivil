package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class TestMetaFunctions extends TestCaseWithEnv {
    MutableRuleApplication ra;
    MetaEvaluator eval;
    
    protected void setUp() throws Exception {
        env = new Environment("wrapped_for_skolem", DEFAULT_ENV);
        ra = new MutableRuleApplication();
        eval = new MetaEvaluator(ra, env);
    }
    
    private void assertEvalsTo(String t1, String t2) throws Exception {
        Term t = makeTerm(t1);
        assertEquals(makeTerm(t2), eval.evalutate(t)); 
    }

    public void testSubst() throws Exception {
        assertEvalsTo("$$subst(i2, 3, i1+i2)", "i1+3");
    }
    
    public void testBoundSubst() throws Exception {
        assertEvalsTo("(\\bind x; $$subst(x, 3, x + (\\bind x; x*2)))",
                "(\\bind x as int; 3 + (\\bind x; x*2))");    
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