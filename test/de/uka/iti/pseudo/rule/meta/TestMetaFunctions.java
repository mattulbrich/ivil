package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class TestMetaFunctions extends TestCaseWithEnv {
    MutableRuleApplication ra;
    MetaEvaluator eval;
    
    protected void setUp() throws Exception {
        loadEnv();
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
        
        assertEquals(makeTerm("sk1 as int"), result);
        assertEquals(makeTerm("sk1 as int"), result2);
    }
    
    public void testSkolemDifferent() throws Exception {
        Term t = makeTerm("$$skolem(1)");
        Term result = eval.evalutate(t);
        Term t2 = makeTerm("$$skolem(true)");
        Term result2 = eval.evalutate(t2);
        
        assertEquals(makeTerm("sk1 as int"), result);
        assertEquals(makeTerm("sk2 as bool"), result2);
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
    
    public void testIntEvalNegated() throws Exception {
        assertEvalsTo("$$intEval((-2)+(-3))", "-5");
    }
    
    public void testIncPrg() throws Exception {
        assertEvalsTo("$$incPrg([1])", "[2]");
        assertEvalsTo("$$incPrg([[100 || 99 := end true]])", "[101 || 99 := end true]");
    }
    
    public void testJmpPrg() throws Exception {
        assertEvalsTo("$$jmpPrg([1], 0)", "[0]");
        assertEvalsTo("$$jmpPrg([[100 || 99 := end true]], 99)", "[99 || 99 := end true]");
    }
    
}