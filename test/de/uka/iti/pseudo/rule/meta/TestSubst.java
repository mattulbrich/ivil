package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.term.Term;

public class TestSubst extends TestCaseWithEnv {

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
}