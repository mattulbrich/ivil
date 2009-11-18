package de.uka.iti.pseudo.auto;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.Term;

public class TestSMTLibTranslator extends TestCaseWithEnv {

    public void testQuantifiers() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        Term t = makeTerm("(\\forall x; x > 0)");
        assertEquals("(forall (?x Int) (> ?x 0))", trans.translate(t));
        
        t = makeTerm("(\\exists y; arb <= y)");
        assertEquals("(exists (?y Int) (<= extra.arb..Int ?y))", trans.translate(t));
    }
    
    // formulas and terms are different in SMTLIB
    public void testFormTerm() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        Term t = makeTerm("(\\forall b; id(b) -> b)");
        assertEquals("(forall (?b Bool) (implies (= (extra.id.Bool.Bool ?b) termTrue) (= ?b termTrue)))", trans.translate(t));
    }
    
    public void testArithmetic() throws Exception {
        
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        String[] ops = { "+", "-", "<", "<=", ">", ">=" };
        
        for (String op : ops) {
            Term t = makeTerm("1" + op + "2");
            assertEquals("(" + op + " 1 2)" , trans.translate(t));
        }
    }
    
    public void testUnknown() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        assertEquals("unknown0", trans.translate(makeTerm("[0;P]")));
        assertEquals("unknown1", trans.translate(makeTerm("{i1:=0}i2")));
        assertEquals("unknown2", trans.translate(makeTerm("{i1:=0}b2")));
        
        assertTrue(trans.extrafuncs.contains("(unknown0 Bool)"));
        assertTrue(trans.extrafuncs.contains("(unknown1 Int)"));
        assertTrue(trans.extrafuncs.contains("(unknown2 Bool)"));
    }
    
    public void testUninterpreted() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
        
        assertEquals("extra.arb..poly_int_poly_int_int__", trans.translate(makeTerm("arb as poly(int, poly(int,int))")));
        assertEquals("(extra.f.Int.Int 5)", trans.translate(makeTerm("f(5) as int")));
        assertEquals("extra.arb..Bool", trans.translate(makeTerm("arb as bool")));
        
    }
    
    public void testCond() throws Exception {
        SMTLibTranslator trans = new SMTLibTranslator(env);
    
        assertEquals("(ite (= extra.b1..Bool termTrue) extra.b1..Bool extra.b2..Bool)", trans.translate(makeTerm("cond(b1, b1, b2)")));
        assertEquals("(ite (> 5 4) 3 2)", trans.translate(makeTerm("cond(5>4, 3, 2)")));
    }
    
}
