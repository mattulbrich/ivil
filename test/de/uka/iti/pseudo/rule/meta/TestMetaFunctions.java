package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class TestMetaFunctions extends TestCaseWithEnv {
    
    protected void setUp() throws Exception {
        loadEnv();
    }

    public void testSubst() throws Exception {
        
        MutableRuleApplication ra = new MutableRuleApplication(); 
        
        MetaEvaluator eval = new MetaEvaluator(ra, env);
        
        Term t = makeTerm("$$subst(i2, 3, i1+i2)");
        Term result = eval.evalutate(t);
        
        assertEquals(makeTerm("i1+3"), result);
        
    }
    
    public void testBoundSubst() throws Exception {
        
        MutableRuleApplication ra = new MutableRuleApplication(); 
        
        MetaEvaluator eval = new MetaEvaluator(ra, env);
        
        Term t = makeTerm("(\\bind x; $$subst(x, 3, x + (\\bind x; x*2)))");
        Term result = eval.evalutate(t);
        
        assertEquals( makeTerm("(\\bind x as int; 3 + (\\bind x; x*2))"), result);
    }
    
    public void testSkolem() throws Exception {
        
        MutableRuleApplication ra = new MutableRuleApplication(); 
        
        MetaEvaluator eval = new MetaEvaluator(ra, env);
        
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term result2 = eval.evalutate(t);
        
        assertEquals(makeTerm("sk1 as int"), result);
        assertEquals(makeTerm("sk1 as int"), result2);
    }
    
    public void testSkolemDifferent() throws Exception {
        
        MutableRuleApplication ra = new MutableRuleApplication(); 
        
        MetaEvaluator eval = new MetaEvaluator(ra, env);
        
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term t2 = makeTerm("$$skolem(true)");
        Term result2 = eval.evalutate(t2);
        
        assertEquals(makeTerm("sk1 as int"), result);
        assertEquals(makeTerm("sk2 as bool"), result2);
    }
    
    public void testSkolemReplay() throws Exception {
        
        MutableRuleApplication ra = new MutableRuleApplication(); 
        ra.getProperties().put("skolemName(1 as int)", "sk100");
        MetaEvaluator eval = new MetaEvaluator(ra, env);
        
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term result2 = eval.evalutate(t);
        
        assertEquals(makeTerm("sk100 as int"), result);
        assertEquals(makeTerm("sk100 as int"), result2);
    }
    
    public void testIntEval() throws Exception {
        MutableRuleApplication ra = new MutableRuleApplication(); 
        MetaEvaluator eval = new MetaEvaluator(ra, env);
        
        Term t = makeTerm("$$intEval(2+3)");
        assertEquals(makeTerm("5"), eval.evalutate(t));
        
        t = makeTerm("$$intEval(2-3)");
        assertEquals(makeTerm("-1"), eval.evalutate(t));
        
        t = makeTerm("$$intEval(i1+3)");
        try {
            Term result = eval.evalutate(t);
            fail("should fail but is " + result);
        } catch(TermException ex) {}
    }
    
    public void testIntEvalNegated() throws Exception {
        MutableRuleApplication ra = new MutableRuleApplication(); 
        MetaEvaluator eval = new MetaEvaluator(ra, env);
        
        Term t = makeTerm("$$intEval((-2)+(-3))");
        assertEquals(makeTerm("-5"), eval.evalutate(t));
    }
    
}